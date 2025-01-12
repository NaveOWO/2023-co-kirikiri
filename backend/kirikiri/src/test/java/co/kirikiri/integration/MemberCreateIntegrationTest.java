package co.kirikiri.integration;

import static co.kirikiri.integration.fixture.MemberAPIFixture.요청을_받는_회원가입;
import static org.assertj.core.api.Assertions.assertThat;

import co.kirikiri.integration.helper.InitIntegrationTest;
import co.kirikiri.service.dto.ErrorResponse;
import co.kirikiri.service.dto.member.request.GenderType;
import co.kirikiri.service.dto.member.request.MemberJoinRequest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

class MemberCreateIntegrationTest extends InitIntegrationTest {

    @Test
    void 정상적으로_회원가입을_성공한다() {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", "password12!@#$%", "hello", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "abcdefghijklmnopqrstu"})
    void 아이디_길이가_틀린_경우_회원가입에_실패한다(final String 회원_아이디) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest(회원_아이디, "password12!", "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(new TypeRef<>() {
        });
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 아이디입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Abcd", "abcdefghijklmnopqrsT", "가나다라"})
    void 아이디에_허용되지_않은_문자가_들어온_경우_회원가입에_실패한다(final String 회원_아이디) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest(회원_아이디, "password12!", "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 아이디입니다.");
    }

    @Test
    void 아이디가_중복된_경우_회원가입에_실패한다() {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", "password12!", "hello", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));
        요청을_받는_회원가입(회원가입_요청);

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(에러_메세지.message()).isEqualTo("이미 존재하는 아이디입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcde1!", "abcdefghijklmn12"})
    void 비밀번호_길이가_틀린_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", password, "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 비밀번호입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdef1/", "abcdefghij1₩", "abcdefgH1!"})
    void 비밀번호에_허용되지_않은_문자가_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", password, "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 비밀번호입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefgh", "abcdefghijkl"})
    void 비밀번호에_영소문자만_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", password, "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 비밀번호입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "12345678910"})
    void 비밀번호에_숫자만_들어온_경우_회원가입에_실패한다(final String password) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", password, "nickname", "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 비밀번호입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "abcdefghi"})
    void 닉네임_길이가_틀린_경우_회원가입에_실패한다(final String nickname) {
        //given
        final MemberJoinRequest 회원가입_요청 = new MemberJoinRequest("ab12", "password12!@#$%", nickname, "010-1234-5678",
                GenderType.MALE, LocalDate.of(2023, Month.JULY, 12));

        //when
        final ExtractableResponse<Response> 회원가입_응답 = 요청을_받는_회원가입(회원가입_요청);

        //then
        final ErrorResponse 에러_메세지 = 회원가입_응답.as(ErrorResponse.class);
        assertThat(회원가입_응답.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(에러_메세지.message()).isEqualTo("제약 조건에 맞지 않는 닉네임입니다.");
    }
}
