package com.wildboar.web.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.wildboar.domain.Student;
import com.wildboar.messaging.Greeting;
import com.wildboar.messaging.ProducerChannel;
import com.wildboar.messaging.StudentCheckIn;
import com.wildboar.repository.StudentRepository;

@RestController
@RequestMapping("/api")
public class ProducerResource {

	private final Logger log = LoggerFactory.getLogger(ProducerResource.class);
	
	private MessageChannel channel;
	private StudentRepository studentRepository;

	public ProducerResource(ProducerChannel channel, StudentRepository studentRepository) {
	    this.channel = channel.messageChannel();
		this.studentRepository = studentRepository;
	}

	@GetMapping("/greetings/{count}")
	@Timed
	public void produce(@PathVariable int count) {
		while (count > 0) {
			channel.send(MessageBuilder.withPayload(new Greeting().setMessage("Hello world!: " + count)).build());
			count--;
		}
	}

	@PostMapping("/notify")
	@Timed
	public void inform(@Valid @RequestBody StudentMessage studentMessage) {
			log.info("Receive GET request with studentId:" + studentMessage.getStudentId());
			final Student student = studentRepository.findByStudentId(studentMessage.getStudentId());
			channel.send(MessageBuilder.withPayload(this.buildMessage(student)).build());
	}
	
	private StudentCheckIn buildMessage(Student student) {
		final StudentCheckIn studentCheckIn = new StudentCheckIn();
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		final LocalDateTime now = LocalDateTime.now();  
		studentCheckIn.setStudentId(student.getStudentId());
		studentCheckIn.setCheckedIn(dtf.format(now));

		//final Greeting greeting = new Greeting();		
		//final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		//final LocalDateTime now = LocalDateTime.now();  
		//final String message = "ID: " + student.getStudentId().toString() + " " + student.getName() + " " + student.getSurname() + " " + dtf.format(now); 
		//greeting.setMessage(message); 
		return studentCheckIn;
	}
}
