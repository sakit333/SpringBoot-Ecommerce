package com.s13sh.myshop.service.implementation;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.s13sh.myshop.dao.CustomerDao;
import com.s13sh.myshop.dto.Customer;
import com.s13sh.myshop.helper.AES;
import com.s13sh.myshop.helper.MailSendingHelper;
import com.s13sh.myshop.service.CustomerService;

import jakarta.servlet.http.HttpSession;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerDao customerDao;

	@Autowired
	MailSendingHelper mailHelper;

	@Override
	public String save(Customer customer, BindingResult result) {
		if (customerDao.checkEmailDuplicate(customer.getEmail()))
			result.rejectValue("email", "error.email", "* Account Already Exists with this Email");
		if (customerDao.checkMobileDuplicate(customer.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists with this Mobile");

		if (result.hasErrors())
			return "Signup";
		else {
			customer.setPassword(AES.encrypt(customer.getPassword(), "123"));
			customer.setRole("USER");
			customerDao.save(customer);
			return "redirect:/send-otp/" + customer.getId();
		}
	}

	@Override
	public String verifyOtp(int id, int otp, ModelMap map, HttpSession session) {
		Customer customer = customerDao.findById(id);
		System.out.println("*******2********");
		if (customer.getOtp() == otp) {
			customer.setVerified(true);
			customerDao.save(customer);
			session.setAttribute("successMessage", "Account Created Success");
			return "redirect:/signin";
		} else {
			map.put("failMessage", "Invalid Otp, Try Again!");
			map.put("id", id);
			return "VerifyOtp";
		}
	}

	@Override
	public String sendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		// mailHelper.sendOtp(customer);
		map.put("id", id);
		map.put("successMessage", "Otp Sent Success");
		return "VerifyOtp";
	}

	@Override
	public String resendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		// mailHelper.resendOtp(customer);
		map.put("id", id);
		map.put("successMessage", "Otp Resent Success");
		return "VerifyOtp";
	}

	@Override
	public String login(String email, String password, ModelMap map, HttpSession session) {
		Customer customer = customerDao.findByEmail(email);
		if (customer == null)
			session.setAttribute("failMessage", "Invalid Email");
		else {
			if (AES.decrypt(customer.getPassword(), "123").equals(password)) {
				if (customer.isVerified()) {
					session.setAttribute("customer", customer);
					session.setAttribute("successMessage", "Login Success");
					return "redirect:/";
				} else {
					return resendOtp(customer.getId(), map);
				}
			} else
				session.setAttribute("failMessage", "Invalid Password");
		}
		return "Login";
	}

}
