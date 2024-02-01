package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament_bet")
public class TournamentBet {
  @Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "tournament_id")
	private Long tournamentId;

	@Column(name = "bet_amount")
	private Integer betAmount;

	@EqualsAndHashCode.Exclude
	@Column(name = "bet_game")
	private String betGame;

  @ToString.Exclude
	@Column(name = "is_skin_game")
	private boolean isSkinGame;

  @ToString.Exclude
	@Column(name = "is_ctp_game")
	private boolean isCTPGame;

}