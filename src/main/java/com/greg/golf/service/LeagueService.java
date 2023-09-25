package com.greg.golf.service;

import com.greg.golf.entity.*;
import com.greg.golf.error.*;
import com.greg.golf.repository.LeagueMatchRepository;
import com.greg.golf.repository.LeaguePlayerRepository;
import com.greg.golf.repository.LeagueRepository;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.RoleVerification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service("leagueService")
public class LeagueService {

    private final LeagueRepository leagueRepository;

    private final PlayerRepository playerRepository;

    private final LeaguePlayerRepository leaguePlayerRepository;

    private final LeagueMatchRepository leagueMatchRepository;

    @Transactional
    public League addLeague(League league) {

        return leagueRepository.save(league);
    }

    @Transactional(readOnly = true)
    public List<League> findAllLeagues() {
        return leagueRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional
    public void addPlayer(LeaguePlayer leaguePlayer) throws DuplicatePlayerInLeagueException {

        var league = leagueRepository.findById(leaguePlayer.getLeagueId()).orElseThrow();
        // only league owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to add player by unauthorized user");

        //check if player exists
        var player = playerRepository.findById(leaguePlayer.getPlayerId()).orElseThrow();

        //prepare data to save
        leaguePlayer.setNick(player.getNick());

        // save entity
        // trow exception if player has been already added to the tournament
        try {
            leaguePlayerRepository.save(leaguePlayer);
        } catch (Exception ex) {
            throw new DuplicatePlayerInLeagueException();
        }
    }

    @Transactional
    public void deletePlayers(Long leagueId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to delete league player result by unauthorized user");

        // remove only if tournaments does not have any results
        //TO DO

        //if (tournamentResultRepository.findByTournament(tournament).isEmpty()) {
            leaguePlayerRepository.deleteByLeagueId(leagueId);
        //} else {
        //    throw new DeleteTournamentPlayerException();
        //}

    }

    @Transactional
    public void deletePlayer(Long leagueId, long playerId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to delete league player result by unauthorized user");

        var player = new Player();
        player.setId(playerId);

        // remove only if tournaments does not have any results
        //TO DO
        //if (tournamentResultRepository.findByPlayerAndTournament(player, tournament).isEmpty()) {
            leaguePlayerRepository.deleteByLeagueIdAndPlayerId(leagueId, playerId);
        //} else {
        //    throw new DeleteTournamentPlayerException();
        //}
    }

    @Transactional
    public List<LeaguePlayer> getLeaguePlayers(Long leagueId) {

        return leaguePlayerRepository.findByLeagueId(leagueId);

    }

    @Transactional
    public void closeLeague(Long leagueId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to close league by unauthorized user");

        // set close flag
        league.setStatus(League.STATUS_CLOSE);
        leagueRepository.save(league);
    }

    @Transactional
    public List<LeagueMatch> getMatches(Long leagueId) {

        return leagueMatchRepository.findByLeagueId(leagueId);

    }

    public void addMatch(LeagueMatch leagueMatch) throws DuplicateMatchInLeagueException, MatchResultForNotLeaguePleayerException {

        var league = leagueRepository.findById(leagueMatch.getLeagueId()).orElseThrow();

        // only league owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to add player by unauthorized user");

        // match cannot be added twice
        if (!leagueMatchRepository
                .findByWinnerIdAndLooserIdAndLeagueId(leagueMatch.getWinnerId(), leagueMatch.getLooserId(), leagueMatch.getLeagueId()).isEmpty()) {
            throw new DuplicateMatchInLeagueException();
        }

        // winner and looser must be league players

        if (leaguePlayerRepository.findByLeagueIdAndPlayerId(league.getId(), leagueMatch.getLooserId()).isEmpty() ||
                leaguePlayerRepository.findByLeagueIdAndPlayerId(league.getId(), leagueMatch.getWinnerId()).isEmpty()) {
            throw new MatchResultForNotLeaguePleayerException();
        }

        leagueMatchRepository.save(leagueMatch);
    }

    @Transactional
    public void deleteMatch(Long leagueId, Long winnerId, Long looserId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), "Attempt to delete league player result by unauthorized user");

        leagueMatchRepository.deleteByLeagueIdAndWinnerIdAndLooserId(leagueId, winnerId, looserId);

    }

}
