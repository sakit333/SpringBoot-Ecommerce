package com.s13sh.myshop.service;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.s13sh.myshop.dto.Customer;

import jakarta.servlet.http.HttpSession;

public interface CustomerService {
	String save(Customer customer, BindingResult result);

	String verifyOtp(int id, int otp, ModelMap map, HttpSession session);

	String sendOtp(int id, ModelMap map);

	String resendOtp(int id, ModelMap map);

	String login(String email, String password, ModelMap map, HttpSession session);
}
