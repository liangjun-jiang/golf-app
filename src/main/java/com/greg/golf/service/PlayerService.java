package com.greg.golf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.greg.golf.captcha.ICaptchaService;
import com.greg.golf.configurationproperties.PlayerServiceConfig;
import com.greg.golf.repository.projection.PlayerRoundCnt;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.security.RefreshTokenUtil;
import com.greg.golf.service.helpers.RoleVerification;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "player")
@Service("playerService")
@CacheConfig(cacheNames = { "player" })
public class PlayerService {

	private final PlayerRepository playerRepository;

	private final PlayerServiceConfig playerServiceConfig;
	private final JwtTokenUtil jwtTokenUtil;
	private final RefreshTokenUtil refreshTokenUtil;
	private final ICaptchaService captchaService;
	private final PasswordEncoder bCryptPasswordEncoder;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public GolfUserDetails authenticatePlayer(Player player) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(player.getNick(), player.getPassword()));
		log.debug("Authentication completed");
		return loadUserAndUpdate(player.getNick());
	}

	public String generateJwtToken(GolfUserDetails userDetails) {
		return jwtTokenUtil.generateToken(userDetails);
	}

	public String generateRefreshToken(GolfUserDetails userDetails) {
		return refreshTokenUtil.generateToken(userDetails);
	}

	@Transactional
	public void addPlayer(Player player) {
		captchaService.processResponse(player.getCaptcha());

		player.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));

		save(player);
	}

	@Transactional
	public Player addPlayerOnBehalf(Player player) {

		player.setPassword(bCryptPasswordEncoder.encode( playerServiceConfig.getTempPwd()));

		return save(player);
	}

	@CacheEvict
	@Transactional
	public void delete(Long id) {

		RoleVerification.verifyRole(Common.ADMIN, "Attempt to delete player by unauthorized user");

		var player = new Player();
		player.setId(id);

		playerRepository.delete(player);
	}

	private Player save(Player player) {

		try {
			player.setRole(Common.ROLE_PLAYER_REGULAR);
			player.setModified(false);
			return playerRepository.save(player);
		} catch (Exception e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				throw new PlayerNickInUseException();
			}
			throw e;
		}

	}

	private GolfUserDetails loadUserAndUpdate(String playerName) {

		Player player = playerRepository.findPlayerByNick(playerName)
				.orElseThrow(() -> new UsernameNotFoundException("User " + playerName + " not found"));

		// clear modify flag if user has been changed
		if (Boolean.TRUE.equals(player.getModified())) {

			player.setModified(false);
			playerRepository.save(player);
			log.debug("Cleared modified flag for user " + playerName);
		}

		log.info("Creating user details for " + playerName);

		return new GolfUser(player.getNick(), player.getPassword(), new ArrayList<>(), player);
	}

	@CacheEvict(value = "player", key = "#player.id")
	public void cacheEvict(@NonNull Player player) {
		log.debug("Cache evict called");
	}

	@Transactional(readOnly = true)
	public GolfUserDetails loadUserById(Long id) {

		Player player = playerRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User " + id + " not found"));

		log.info("Creating user details for " + id);

		return new GolfUser(player.getNick(), player.getPassword(),	new ArrayList<>(), player);
	}

	@Cacheable(value = "player", key = "#id")
	@Transactional(readOnly = true)
	public Optional<Player> getPlayer(Long id) {
		log.debug("Get player called");
		return playerRepository.findById(id);
	}

	@CacheEvict(value = "player", key = "#player.id")
	@Transactional
	public Player update(@NonNull Player player) {

		var persistedPlayer = playerRepository.getById(player.getId());

		if (player.getWhs() != null) {
			persistedPlayer.setWhs(player.getWhs());
			log.info("Handicap changed by player");
		}
		if (player.getPassword() != null && !player.getPassword().equals("")) {
			persistedPlayer.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
			log.info("Password changed by player");
		}

		playerRepository.save(persistedPlayer);

		return persistedPlayer;
	}

	@CacheEvict(value = "player", key = "#player.id")
	@Transactional
	public void updatePlayerOnBehalf(@NonNull Player player) {

		var persistedPlayer = playerRepository.findById(player.getId()).orElseThrow();
		boolean changed = false;

		if (player.getWhs() != null && !player.getWhs().equals(persistedPlayer.getWhs()) ) {
			persistedPlayer.setWhs(player.getWhs());
			changed = true;
		}

		if (player.getNick() != null && !player.getNick().equals(persistedPlayer.getNick()) ) {
			persistedPlayer.setNick(player.getNick());
			changed = true;
		}

		if (player.getSex() != null && !player.getSex().equals(persistedPlayer.getSex()) ) {
			persistedPlayer.setSex(player.getSex());
			changed = true;
		}

		if (changed) {
			persistedPlayer.setModified(true);
			playerRepository.save(persistedPlayer);
			log.debug("player changes saved for " + persistedPlayer.getNick());
		} else {
			log.warn("nothing to update for player " + persistedPlayer.getNick());
		}
	}

	@Transactional
	public void resetPassword(Player player) {

		if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()
				.equals(Common.ADMIN)) {

			if (player.getPassword() != null && !player.getPassword().equals("")) {
				Player persistedPlayer = playerRepository.findPlayerByNick(player.getNick()).orElseThrow();
				persistedPlayer.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
				playerRepository.save(persistedPlayer);
			}

		} else {
			log.error("Attempt to reset password by unauthorized user");
			throw new UnauthorizedException();
		}
	}

	@Transactional(readOnly = true)
	public Player getPlayerForNick(String nick) {

		Optional<Player> player = playerRepository.findPlayerByNick(nick);

		if (player.isEmpty()) {
			return null;
		}

		return player.get();
	}

	@Transactional
	public List<PlayerRoundCnt> getPlayerRoundCnt() {

		RoleVerification.verifyRole(Common.ADMIN, "Attempt to get player statistic by unauthorized user");

		return playerRepository.getPlayerRoundCnt();
	}
}
