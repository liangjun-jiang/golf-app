package com.greg.golf.repository;

import com.greg.golf.entity.TournamentBetGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentBetGameRepository extends JpaRepository<TournamentBetGame, Long> {

    void deleteByTournamentId(Long tournamentId);

    List<TournamentBetGame> findByTournamentId(Long tournamentId);

}