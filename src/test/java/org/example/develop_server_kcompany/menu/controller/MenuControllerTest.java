package org.example.develop_server_kcompany.menu.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.develop_server_kcompany.common.exception.CustomException;
import org.example.develop_server_kcompany.common.exception.ErrorCode;
import org.example.develop_server_kcompany.common.exception.GlobalExceptionHandler;
import org.example.develop_server_kcompany.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MenuControllerTest 테스트 클래스입니다.
 * <p>
 * 메뉴 목록 조회 API에서 발생하는 예외가
 * GlobalExceptionHandler를 통해 올바른 HTTP 응답으로 변환되는지 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 13.
 */
@WebMvcTest(MenuController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class MenuControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MenuService menuService;

	@Test
	@DisplayName("메뉴 목록 조회 요청 시 올바르지 않은 요청을 보낸 경우")
	void getMenus_FailCase_InvalidInputTest() throws Exception {
		// given
		given(menuService.getMenus())
			.willThrow(new CustomException(ErrorCode.INVALID_REQUEST));

		// when & then
		mockMvc.perform(get("/api/menus"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."));

	}
}