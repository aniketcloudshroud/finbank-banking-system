package com.finbank;

import com.finbank.entity.*;
import com.finbank.repository.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;

import java.time.*;

@SpringBootApplication
public class FinbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinbankApplication.class, args);
    }

}
