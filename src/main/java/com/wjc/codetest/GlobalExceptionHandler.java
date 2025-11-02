package com.wjc.codetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 문제: 예외 핸들러가 정의되어 있지만 실제 서비스 코드에서 사용되지 않음.
 *      서비스 단에서 RuntimeException을 던지지만 컨트롤러 어드바이스가 감지하지 못함.
 * 원인: @ControllerAdvice의 basePackages 설정이 "com.wjc.codetest.product.controller"로 한정되어 있음.
 *       ProductService가 다른 패키지(com.wjc.codetest.product.service)에 위치해 예외 전파가 잡히지 않음.
 * 개선안: basePackages를 제거하거나 상위 패키지(com.wjc.codetest)로 변경해 전역 적용.
 *        프로젝트 루트 패키지를 지정해 모든 컨트롤러에 공통 적용.
 */

@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> runTimeException(Exception e) {
        log.error("status :: {}, errorType :: {}, errorCause :: {}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "runtimeException",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
