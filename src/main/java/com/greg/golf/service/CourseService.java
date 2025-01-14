package com.greg.golf.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.greg.golf.error.TeeAlreadyExistsException;
import com.greg.golf.service.helpers.RoleVerification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.configurationproperties.CourseServiceConfig;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.CourseRepository;
import com.greg.golf.repository.CourseTeeRepository;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.HoleRepository;


import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
//@ConfigurationProperties(prefix = "course")
@Service("courseService")
public class CourseService {

	private final CourseServiceConfig courseServiceConfig;
	private final CourseRepository courseRepository;
	private final CourseTeeRepository courseTeeRepository;
	private final FavouriteCourseRepository favouriteCourseRepository;
	private final HoleRepository holeRepository;

	@Lazy
	private final CourseService self;

	@Transactional()
	public void addTee(CourseTee courseTee, Long courseId) throws TeeAlreadyExistsException {

		Course course = courseRepository.findById(courseId).orElseThrow();

		// not to add tee if the same sex and colour already exist
		if (course.getTees()
				.stream()
				.anyMatch(tee -> tee.getSex() == courseTee.getSex() &&
						Objects.equals(tee.getTee(), courseTee.getTee()) &&
						Objects.equals(tee.getTeeType(), courseTee.getTeeType()))) {
			throw new TeeAlreadyExistsException();
		}

		courseTee.setCourse(course);
		courseTeeRepository.save(courseTee);
	}


	@Transactional(readOnly = true)
	public List<Course> searchForCourses(String courseName) {

		if (courseName.length() < courseServiceConfig.getMinSearchLength()) {
			throw new TooShortStringForSearchException();
		}

		return courseRepository.findByHistoricalAndNameContainingIgnoreCase(false, courseName);
	}

	@Transactional()
	public long deleteFromFavourites(Course course, Long playerId) {

		var player = new Player();
		player.setId(playerId);

		return favouriteCourseRepository.deleteByPlayerAndCourse(player, course);
	}

	@Transactional()
	public void addToFavourites(Course course, Long playerId) {

		var player = new Player();
		player.setId(playerId);
		var favouriteCourse = new FavouriteCourse();
		favouriteCourse.setPlayer(player);
		favouriteCourse.setCourse(course);

		favouriteCourseRepository.save(favouriteCourse);
	}

	@Transactional(readOnly = true)
	public List<Course> list() {
		return courseRepository.findByHistoricalOrderByNameAsc(false);
	}

	@Transactional(readOnly = true)
	public List<Course> listFavourites(Long playerId) {

		var player = new Player();
		player.setId(playerId);

		return self.listFavourites(player);
	}

	@Transactional(readOnly = true)
	public List<Course> listFavourites(Player player) {
		List<FavouriteCourse> favouriteCourses = favouriteCourseRepository.findByPlayer(player);

		return favouriteCourses.stream().map(FavouriteCourse::getCourse).toList();
	}

	@Transactional(readOnly = true)
	public List<Hole> getHoles(Course course) {
		return holeRepository.findByCourse(course);
	}

	@Transactional(readOnly = true)
	public Optional<Course> getCourse(Long id) {
		return courseRepository.findById(id);
	}

	@Transactional
	public Course save(Course course) {

		course.getHoles().forEach(h -> h.setCourse(course));
		course.getTees().forEach(h -> h.setCourse(course));
		course.setHistorical(false);

		return courseRepository.save(course);
	}

	@Transactional
	public void delete(Long id) {

		RoleVerification.verifyRole(Common.ADMIN, "Attempt to delete course by unauthorized user");

		courseRepository.deleteById(id);

	}

	@Transactional(readOnly = true)
	public List<CourseTee> getTees(Long id) {

		var course = new Course();
		course.setId(id);

		return courseTeeRepository.findByCourse(course);

	}

	@Transactional(readOnly = true)
	public Optional<CourseTee> getTeeById(Long id) {

		return courseTeeRepository.findById(id);

	}

	@Transactional(readOnly = true)
	public List<Course> getSortedCourses(Integer pageNo) {

		return courseRepository.findByHistoricalOrderByNameAsc(false,
				PageRequest.of(pageNo, courseServiceConfig.getPageSize()));
	}

	@Transactional()
	public void moveToHistoryCurse(Long courseId) {

		if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()
				.equals(Common.ADMIN)) {
			// set historical flag for course
			var course = courseRepository.findById(courseId).orElseThrow();
			course.setHistorical(true);
			courseRepository.save(course);

			// remove course from favorites
			favouriteCourseRepository.deleteByCourse(course);
		} else {
			log.error("Attempt to move course to history by unauthorized user");
			throw new UnauthorizedException();
		}
	}
}
