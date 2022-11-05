package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PlayerRegistrationDto extends PlayerCredentialsDto {

	@NotNull
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = WRITE_ONLY)
	private Boolean sex;

	@NotNull
	@Min(value = -5)
	@Max(value = 54)
	@Schema(description = "Player handicap", example = "38.5", accessMode = WRITE_ONLY, minimum="-5", maximum="54")
	private Float whs;

	@NotNull
	@Schema(description = "Google captcha", example = "03AG...", accessMode = WRITE_ONLY)
	private String captcha;
}
