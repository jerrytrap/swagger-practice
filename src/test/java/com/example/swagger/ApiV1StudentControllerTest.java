package com.example.swagger;

import com.example.swagger.domain.student.ApiV1StudentController;
import com.example.swagger.domain.student.Student;
import com.example.swagger.domain.student.StudentService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1StudentControllerTest {
    @Autowired
    private StudentService studentService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/join")
                                .content("""
                                        {
                                            "username": "new user",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent()
                                ).contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("무명님 환영합니다. 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.createDate").isString())
                .andExpect(jsonPath("$.data.modifyDate").isString())
                .andExpect(jsonPath("$.data.nickname").value("무명"));

        Student student = studentService.findStudentByName("new user").get();
        assertThat(student.getNickname()).isEqualTo("무명");
    }

    @Test
    @DisplayName("로그인")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/login")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Student student = studentService.findStudentByName("user1").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("user1님 환영합니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.item").exists())
                .andExpect(jsonPath("$.data.item.id").value(student.getId()))
                .andExpect(jsonPath("$.data.item.createDate").value(Matchers.startsWith(student.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.item.modifyDate").value(Matchers.startsWith(student.getModifiedDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.item.nickname").value("이름1"))
                .andExpect(jsonPath("$.data.apiKey").value(student.getApiKey()));
    }

    @Test
    @DisplayName("회원가입 without username, password, nickname")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/join")
                                .content("""
                                        {
                                            "username": "",
                                            "password": "",
                                            "nickname": ""
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        nickname-NotBlank-must not be blank
                        password-NotBlank-must not be blank
                        username-NotBlank-must not be blank
                        """.stripIndent().trim()));
    }

    @Test
    @DisplayName("회원가입 시 이미 사용중인 username, 409")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/join")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("해당 username은 이미 사용중입니다."));
    }

    @Test
    @DisplayName("로그인, without username")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/login")
                                .content("""
                                        {
                                            "username": "",
                                            "password": "1234"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("username-NotBlank-must not be blank"));
    }

    @Test
    @DisplayName("로그인, without password")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/login")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": ""
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("password-NotBlank-must not be blank"));
    }

    @Test
    @DisplayName("로그인, with wrong username")
    void t7() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/login")
                                .content("""
                                        {
                                            "username": "user0",
                                            "password": "1234"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("로그인, with wrong password")
    void t8() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/students/login")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("내 정보, with user1")
    void t9() throws Exception {
        Student student = studentService.findStudentByName("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/students/me")
                                .header("Authorization", "Bearer " + student.getApiKey())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.createDate").value(student.getCreateDate().toString()))
                .andExpect(jsonPath("$.modifyDate").value(student.getModifiedDate().toString()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(student.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(student.getModifiedDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.nickname").value(student.getNickname()));
    }

    @Test
    @DisplayName("내 정보, with user2")
    void t10() throws Exception {
        Student student = studentService.findStudentByName("user2").get();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/students/me")
                                .header("Authorization", "Bearer " + student.getApiKey())
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(student.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(student.getModifiedDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.nickname").value(student.getNickname()));
    }
    @Test
    @DisplayName("내 정보, with wrong api key")
    void t11() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/students/me")
                                .header("Authorization", "Bearer wrong-api-key")
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1StudentController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
    }
}