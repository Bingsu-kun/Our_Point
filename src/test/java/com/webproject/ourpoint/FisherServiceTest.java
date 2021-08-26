package com.webproject.ourpoint;

import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.repository.FisherRepository;
import com.webproject.ourpoint.service.FisherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class FisherServiceTest {

  @InjectMocks
  private FisherService fisherService;

  @Spy
  private PasswordEncoder passwordEncoder;

  @Mock
  private FisherRepository fisherRepository;

  @Mock
  private Fisher fisher;

  @Test
  @DisplayName("로그인 검증 테스트")
  private void loginValidationTest() {
    // -------------- null check
    //given
    String status = HttpStatus.BAD_GATEWAY.toString();
    //when
    doReturn(HttpStatus.BAD_REQUEST).when(fisherService).login(null,"test!1234");
    Fisher fisher = fisherService.login(null,"test!1234");
    //then
    assertThat(fisher).isNull();
  }

}
