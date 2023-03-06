package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database

		Customer customer1=new Customer();
		customer1.setMobile(customer.getMobile());
		customer1.setPassword(customer.getPassword());

		customerRepository2.save(customer1);

	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);


	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList=driverRepository2.findAll();

		Driver mainDriver=null;
		int id=Integer.MAX_VALUE;


		for(Driver driver: driverList){
			if(driver.getCab().getAvailable()){
				if(id>driver.getDriverId()){
					id=driver.getDriverId();
					mainDriver=driver;
				}
			}

		}
		if(mainDriver==null){
			throw new Exception("No cab available!");
		}

		mainDriver.getCab().setAvailable(false);


		TripBooking tripBooking=new TripBooking();

		tripBooking.setBill(distanceInKm*10);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		tripBooking.setDriver(mainDriver);
		tripBooking.setCustomer(customerRepository2.findById(customerId).get());

		customerRepository2.findById(customerId).get().getTripBookingList().add(tripBooking);

		customerRepository2.save(customerRepository2.findById(customerId).get());

		mainDriver.getTripBookingList().add(tripBooking);
		driverRepository2.save(mainDriver);

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly


		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		Driver driver=tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);




	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		//tripBooking.setBill(0);

		Driver driver=tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);


	}
}
