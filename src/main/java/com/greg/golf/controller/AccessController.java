package com.greg.golf.controller;

import com.greg.golf.controller.dto.*;
import com.greg.golf.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.greg.golf.entity.Player;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.helpers.GolfUserDetails;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@OpenAPIDefinition(tags = @Tag(name = "Access API"))
public class AccessController extends BaseController {

	private static final String REFRESH_TOKEN = "refreshToken";

	private final PlayerService playerService;
	private final JwtTokenUtil jwtTokenUtil;

	public AccessController(ModelMapper modelMapper, PlayerService playerService, JwtTokenUtil jwtTokenUtil) {
		super(modelMapper);
		this.playerService = playerService;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Authenticate player with given nick name and password. WHS is not relevant.")
	@PostMapping(value = "/rest/Authenticate")
	public ResponseEntity<PlayerDto> authenticatePlayer( @Valid
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerCredentialsDto playerCredentialsDto) {

		log.info("trying to authenticate player: " + playerCredentialsDto.getNick() + " with password *****");

		GolfUserDetails userDetails =playerService.authenticatePlayer(modelMapper.map(playerCredentialsDto, Player.class));

		var responseHeaders = new HttpHeaders();
		responseHeaders.set("Access-Control-Expose-Headers", "Jwt");
		responseHeaders.set("Jwt", playerService.generateJwtToken(userDetails));
		responseHeaders.set("Refresh", playerService.generateRefreshToken(userDetails));

		return new ResponseEntity<>(modelMapper.map(userDetails.getPlayer(), PlayerDto.class), responseHeaders,
				HttpStatus.OK);
	}

	@Tag(name = "Access API")
	@Operation(summary = "Get Social media player data.")
	@GetMapping(value = "/rest/GetSocialPlayer")
	public ResponseEntity<PlayerDto> getSocialPlayer(HttpServletRequest request) {

		String requestTokenHeader = request.getHeader("Authorization");

		String userId = jwtTokenUtil.getUserIdFromToken(requestTokenHeader.substring(7));
		GolfUserDetails userDetails = playerService.loadUserById(Long.valueOf(userId));
		log.info("get data for social player : " + userDetails.getPlayer().getNick());

		var responseHeaders = new HttpHeaders();
		responseHeaders.set("refresh", playerService.generateRefreshToken(userDetails));

		return new ResponseEntity<>(modelMapper.map(userDetails.getPlayer(), PlayerDto.class), responseHeaders, HttpStatus.OK);
	}


	@Tag(name = "Access API")
	@Operation(summary = "Add player.")
	@PostMapping(value = "/rest/AddPlayer")
	public HttpStatus addPlayer(@Valid
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerRegistrationDto playerRegistrationDto) {

		log.info("trying to add player: " + playerRegistrationDto.getNick());

		playerService.addPlayer(modelMapper.map(playerRegistrationDto, Player.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Update player. Only WHS and/or password can be updated.")
	@PatchMapping(value = "/rest/PatchPlayer")
	public HttpStatus updatePlayer( @Valid
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerUpdateDto playerUpdateDto) {

		log.info("trying to update player id: " + playerUpdateDto.getId());

		playerService.update(modelMapper.map(playerUpdateDto, Player.class));

		return HttpStatus.OK;
	}

	@SuppressWarnings({"UnusedReturnValue"})
	@Tag(name = "Access API")
	@Operation(summary = "Administrative task: Reset password.")
	@PatchMapping(value = "/rest/ResetPassword")
	public HttpStatus resetPassword( @Valid
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerCredentialsDto playerCredentialsDto) {

		log.info("trying to reset the password for player: " + playerCredentialsDto.getNick());

		playerService.resetPassword(modelMapper.map(playerCredentialsDto, Player.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Add player on behalf of him. It will have temporary password.")
	@PostMapping(value = "/rest/AddPlayerOnBehalf")
	public ResponseEntity<PlayerUpdateOnBehalfDto> addPlayerOnBehalf( @Valid
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerUpdateOnBehalfDto playerUpdateOnBehalfDto) {

		log.info("trying to add player on behalf: " + playerUpdateOnBehalfDto.getNick() + " with temporary password");

		Player player = playerService.addPlayerOnBehalf(modelMapper.map(playerUpdateOnBehalfDto, Player.class));

		return new ResponseEntity<>(modelMapper.map(player, PlayerUpdateOnBehalfDto.class), HttpStatus.OK);
	}

	@SuppressWarnings("UnusedReturnValue")
	@Tag(name = "Access API")
	@Operation(summary = "Refresh player token.")
	@GetMapping(value = "/rest/Refresh/{id}")
	public ResponseEntity<String> refreshToken(HttpServletRequest request,
			@Parameter(required = true, description = "Id of the player") @PathVariable("id") Long id) {

		log.debug("trying to refresh token for player id: " + id);

		var responseHeaders = new HttpHeaders();

		if (request.getAttribute(REFRESH_TOKEN) != null) {

			updateRefreshHeader(responseHeaders, id);
		}

		return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
	}

	@SuppressWarnings("UnusedReturnValue")
	@Tag(name = "Access API")
	@Operation(summary = "Refresh player JWT on demand")
	@GetMapping(value = "/rest/RefreshToken/{id}")
	public ResponseEntity<String> refreshTokenOnDemand(HttpServletRequest request,
											   @Parameter(required = true, description = "Id of the player") @PathVariable("id") Long id) {

		log.debug("trying to refresh token on demand for player id: " + id);

		var responseHeaders = new HttpHeaders();

		updateRefreshHeader(responseHeaders, id);

		return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
	}

	private void updateRefreshHeader(HttpHeaders responseHeaders, Long id) {

		final GolfUserDetails userDetails = playerService.loadUserById(id);

		responseHeaders.set("Access-Control-Expose-Headers", "Jwt");
		responseHeaders.set("Jwt",  playerService.generateJwtToken(userDetails));
		// regenerate refresh token
		responseHeaders.set("Refresh", playerService.generateRefreshToken(userDetails));

	}


	@Tag(name = "Access API")
	@Operation(summary = "Delete player")
	@PostMapping(value = "/rest/DeletePlayer")
	public HttpStatus deletePlayer(@Valid
			@Parameter(description = "Player id", required = true) @RequestBody IdDto idDto) {

		log.info("trying to delete player with id: " + idDto.getId());

		playerService.delete(idDto.getId());

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Update player by administrator.")
	@PatchMapping(value = "/rest/UpdatePlayerOnBehalf")
	public HttpStatus updatePlayerOnBehalf(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerUpdateOnBehalfDto playerUpdateOnBehalfDto) {

		log.info("trying to update player: " + playerUpdateOnBehalfDto.getNick() + " by admin or other player");
		boolean updateSocial = playerUpdateOnBehalfDto.getUpdateSocial() != null && playerUpdateOnBehalfDto.getUpdateSocial();

		playerService.updatePlayerOnBehalf(modelMapper.map(playerUpdateOnBehalfDto, Player.class), updateSocial);

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Get list of players for nick starting from given string.")
	@PostMapping(value = "/rest/SearchForPlayer")
	public List<PlayerDto> searchForPlayer(
			@Valid @Parameter(description = "PlayerNick DTO object", required = true) @RequestBody PlayerNickDto playerNickDto) {

		log.info("Requested search for player for nick starting with: " + playerNickDto.getNick());

		return mapList(playerService.searchForPlayer(playerNickDto.getNick(), playerNickDto.getPage()), PlayerDto.class);
	}
}
