package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "player_winning_hole")
public class PlayerWinningHole {
  @Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "player_id")
	private Long playerId;

	@ToString.Exclude
	@Column(name = "round_id")
	private Long roundId;

  @Column(name = "tournament_id")
	private Long tournamentId;

  @Column(name = "hole_id")
	private Long holeId;

  @Column(name = "is_skin_hole")
	private boolean isSkinHole;

  @Column(name = "is_ctp_hole")
	private boolean isCTPHole;

  @Column(name = "ctpAmount")
	private Float ctpAmount;

  @Column(name = "skinAmount")
	private Float skinAmount;


}