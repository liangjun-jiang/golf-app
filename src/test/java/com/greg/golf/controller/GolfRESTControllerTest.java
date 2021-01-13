package com.greg.golf.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.greg.golf.controller.GolfRESTController;
import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseTeeDto;
import com.greg.golf.controller.dto.HoleDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class GolfRESTControllerTest {

	private static Player player;
	private static Round round;
	private static Tournament tournament;
	
	@Autowired
	TournamentResultRepository tournamentResultRepository;

	@Autowired
	private GolfRESTController golfRESTController;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository,
			@Autowired TournamentRepository tournamentRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		Set<Player> playerSet = new HashSet<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		round.setRoundDate(new Date(1));
		round.setScoreCard(new ArrayList<ScoreCard>());
		ScoreCard scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setRound(round);
		scoreCard.setStroke(5);
		round.getScoreCard().add(scoreCard);
		scoreCard = new ScoreCard();
		scoreCard.setHole(2);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setRound(round);
		scoreCard.setStroke(4);
		round.getScoreCard().add(scoreCard);
		round = roundRepository.save(round);
		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 1, 1F, 2L, 1, player.getId(), round.getId());

		tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);

		log.info("Set up completed");
	}

	@DisplayName("Gets all tournaments not empty")
	@Transactional
	@Test
	void getTrournamentsTest() {

		List<Tournament> tournaments = this.golfRESTController.getTournaments();
		assertEquals(1, tournaments.size());

	}

	@DisplayName("Gets all tournaments empty")
	@Transactional
	@Test
	void getTrournamentsWithEmptyTest(@Autowired TournamentRepository tournamentRepository) {

		tournamentRepository.deleteAll();
		List<Tournament> tournaments = this.golfRESTController.getTournaments();
		assertEquals(0, tournaments.size());

	}

	@DisplayName("Gets list of holes for course")
	@Transactional
	@Test
	void getListOfHolesTest() {

		List<HoleDto> holeLst = this.golfRESTController.getHoles(1l);

		assertEquals(18, holeLst.size());

	}

	@DisplayName("Add course test")
	@Transactional
	@Test
	void addCourseTest() {

		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);

		List<HoleDto> holeDtoLst = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			HoleDto holeDto = new HoleDto();
			holeDto.setNumber(i + 1);
			holeDto.setPar(4);
			holeDto.setSi(18);
			holeDtoLst.add(holeDto);
		}

		courseDto.setHoles(holeDtoLst);

		List<CourseTeeDto> courseTeeDtoLst = new ArrayList<>();
		CourseTeeDto courseTeeDto = new CourseTeeDto();
		courseTeeDto.setCr(71f);
		courseTeeDto.setSr(78);
		courseTeeDto.setTeeType(1);
		courseTeeDto.setTee("Ladies red 1-18");
		courseTeeDtoLst.add(courseTeeDto);

		courseDto.setTees(courseTeeDtoLst);

		HttpStatus status = this.golfRESTController.addCourse(courseDto);

		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Get tees test")
	@Transactional
	@Test
	void getTeesTest() {

		List<CourseTeeDto> retTees = this.golfRESTController.getTees(1l);

		assertEquals(9, retTees.size());
	}
	
	@DisplayName("Get favourite course test")
	@Transactional
	@Test
	void getFavouriteCourseTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {

		FavouriteCourse fc = new FavouriteCourse();
		Player player = new Player();
		player.setId(1L);
		Course course = new Course();
		course.setId(1L);
		fc.setPlayer(player);
		fc.setCourse(course);
		
		favouriteCourseRepository.save(fc);
		
		List<CourseDto> retCourses = this.golfRESTController.getFavouriteCourses(1l);
		
		assertEquals(1, retCourses.size());
	}
	
	@DisplayName("Get course to favourites")
	@Transactional
	@Test
	void getCourseToFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1l);;
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		HttpStatus status = this.golfRESTController.addCourseToFavourites(1l, courseDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Delete course from favourites")
	@Transactional
	@Test
	void deleteCourseFromFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1l);;
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		this.golfRESTController.addCourseToFavourites(1l, courseDto);
		HttpStatus status = this.golfRESTController.deleteCourseFromFavourites(1l, courseDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository,
			@Autowired TournamentRepository tournamentRepository, @Autowired TournamentResultRepository tr) {

		roundRepository.deleteAll();
		tr.deleteAll();
		tournamentRepository.deleteAll();

		log.info("Clean up completed");

	}
}
