package com.greg.golf.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Slf4j
@Table(name = "tournament_bet_game_result")
public class TournamentBetGameResult {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Tournament tournament;

  @ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Player player;

  @NotNull
	@Column(name = "skins_count")
	private Integer skinsCount;

	@NotNull
	@Column(name = "ctp_count")
	private Integer ctpCount;

  @NotNull
	@Column(name = "total_ctp_amount")
	private Float totalCtpAmount;

	@NotNull
	@Column(name = "skins_amount")
	private Float totalSkinsAmount;

  @EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournament_bet_game_result", orphanRemoval = true)
	private List<PlayerWinningHole> playerWinningHoles;
}