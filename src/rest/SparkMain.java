	package rest;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.post;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


import beans.Address;
import beans.Apartment;
import beans.CommentForApartment;
import beans.ContentOfApartment;
import beans.Guest;
import beans.Host;
import beans.Location;
import beans.Reservation;
import beans.User;
import dto.ApartmentDTO;
import dto.CommentsDTO;
import dto.ContentsOfApartmentDTO;
import dto.HolidaysDTO;
import dto.ReservationDTO;
import dto.UserDTO;
import enums.StatusApartment;
import enums.StatusOfComment;
import enums.StatusReservation;
import enums.TypeOfApartment;
import enums.TypeOfUser;
import spark.Session;
import spark.utils.IOUtils;

public class SparkMain {

	public static UserDTO userDto = new UserDTO();
	public static ApartmentDTO appartmentDto = new ApartmentDTO();
	public static ReservationDTO reservationDto = new ReservationDTO();
	public static ContentsOfApartmentDTO contentsOfApartmentDTO = new ContentsOfApartmentDTO(); 
	public static CommentsDTO commentsDTO = new CommentsDTO();
	public static HolidaysDTO holidaysDTO = new HolidaysDTO();
	
	public static void main(String[] args) throws Exception {
		port(9001);

		staticFiles.externalLocation(new File("./static").getCanonicalPath());


		userDto.loadFile();
		
		appartmentDto.loadFile();
		
		contentsOfApartmentDTO.loadFile();
		
		reservationDto.loadFile();
		commentsDTO.loadFile();
		
		holidaysDTO.loadFile();


		post("/login", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
		
			String a = req.body();
			User userLogin = g.fromJson(a, User.class);
			User user = userDto.loginUser(userLogin.getUserName(), userLogin.getPassword());
				
			if (user == null) {
				res.status(400);
				return res;
			}

			Session ss = req.session(true);
			User userSession = ss.attribute("user");

			if (userSession == null) {
				userSession = user;
				ss.attribute("user", userSession);
			}

			return g.toJson(userSession);

		});


		get("/logout", (req, res) -> {
			res.type("application/json");
			Session ss = req.session(true);
			User user = ss.attribute("user");
			if (user != null) {
				ss.invalidate();
			}
			return true;
		});
		

		post("/registrationGuest", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			String playload = req.body();
			
			Guest user = g.fromJson(playload, Guest.class);
			user.setTypeOfUser(TypeOfUser.GUEST);
			user.setImagePath("/data/profile/profile.jpg");
			user.setIsBlocked(false);
			
			boolean fleg = true;
			for (User u : userDto.getUsers()) {
				if (u.getUserName().equals(user.getUserName())) {
					fleg = false;
					break;
				}
			}

			if (fleg) {
				userDto.getUsers().add(user);
				userDto.saveFile();

				Session ss = req.session(true);
				User userSession = ss.attribute("user");

				if (userSession == null) {
					userSession = user;
					ss.attribute("user", userSession);
				}

			}

			return g.toJson(fleg);
		});
		
		
		
		post("/saveChagesUser", (req,res)->{
			res.type("application/json");
			Gson g = new Gson();
			
			String playload = req.body();
			User user = g.fromJson(playload, User.class);
			
			boolean fleg = false;
			for (User u : userDto.getUsers()) {
				if (u.getUserName().equals(user.getUserName())) {
					fleg = true;
					u.setName(user.getName());
					u.setSurname(user.getSurname());
					if(!user.getPassword().equals("")) {
						u.setPassword(user.getPassword());
					}
				
					break;
				}
			}
			
			if(!fleg) {
				return false;
			}
			
			
			userDto.saveFile();	
			return true;
		});
		
		get("/sesion", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user == null) {
				user = new User();
				user.setTypeOfUser(TypeOfUser.NO_LOGIN);
				return  g.toJson(user);
			}
			
			return g.toJson(user);
		});
		
		get("/validationAcces", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user == null) {
				res.status(403);
				return res;
			}
			
			return true;
		});
		
		get("/validationAccesAdmin", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user == null) {
				res.status(403);
				return res;
			}else if (user.getTypeOfUser() != TypeOfUser.ADMINISTRATOR) {
				res.status(403);
				return res;
			}
			
			return true;
		});
		
		get("/validationAccesHost", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user == null) {
				res.status(403);
				return res;
			}else if (user.getTypeOfUser() != TypeOfUser.HOST) {
				res.status(403);
				return res;
			}
			
			return true;
		});
		
		
		
		
		get("/allUsers", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user != null) {
				if(user.getTypeOfUser() == TypeOfUser.HOST) {
					ArrayList<User> users = new ArrayList<User>();
					for(Reservation reservation : reservationDto.getReservations()) {
						User u = userDto.getUserById(reservation.getIdGuest());
						if(!users.contains(u)) {
							users.add(u);
						}
						
					}
					return g.toJson(users);
				}	
			}
			
			
			
			
			
			return g.toJson(userDto.getUsers());
			
		});
		
		post("/addApartmentPhotos", (request, response) ->{
			request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("static/data/apartments"));			
			if(!Files.exists(Paths.get("static/data/apartments/" + request.queryParams("id")))){
				Files.createDirectory(Paths.get("static/data/apartments/" + request.queryParams("id")));
			}
				
			int i = 0;
			for(Part part : request.raw().getParts()) {
				Path out = Paths.get("static/data/apartments/" + request.queryParams("id") + "/" + i + ".jpg");
				
				try(final InputStream in = part.getInputStream()){
					OutputStream outStream = new FileOutputStream(out.toString());
					IOUtils.copy(in, outStream);
					outStream.close();
					in.close();
					part.delete();
					
					appartmentDto.addImagePath(Integer.parseInt(request.queryParams("id")), "/data/apartments/" + request.queryParams("id") + "/" + i++ + ".jpg");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			appartmentDto.saveFile();
			
			System.out.println("Dodao slike");
				
			return true;
		});
		

		post("/uploadProfileImage", (request, response) -> {
			request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("static/data/profile"));					
			Session ss = request.session(true);
			User user = ss.attribute("user");			
			Part uploadedFile = request.raw().getPart("image");
			
			Path out = Paths.get("static/data/profile/" + user.getUserName() + ".jpg");

		    try(final java.io.InputStream in = uploadedFile.getInputStream()){

		    	OutputStream outStream = new FileOutputStream(out.toString());		 
		    	IOUtils.copy(in, outStream);
		    	
		    	outStream.close();
		    	uploadedFile.delete();
		    	in.close();
		    	 	
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		       
		    return "data/profile/" + user.getUserName() + ".jpg";
			
		}); 

		get("/allAppartmants", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			
			if(user == null || user.getTypeOfUser() == TypeOfUser.GUEST) {
				return g.toJson(appartmentDto.getAllActiveApartment());
			}else if(user.getTypeOfUser() == TypeOfUser.HOST) {
				//ovo vraca samo aktivne
				return g.toJson(appartmentDto.getAllApartmentFromHost(user));
			}else {
				return g.toJson(appartmentDto.getAppartment());
			}
			
			
			
		});
		get("/allAppartmantsPassive", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			
			if(user.getTypeOfUser() == TypeOfUser.HOST) {
				//ovo vraca samo neaktivne od domacina
				return g.toJson(appartmentDto.getAllPassiveApartment(user));
			}
			
			res.status(403);
			return res;
			
		});
		
		
		
		
		
		
		
		
		post("/addApartment", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			String playload = req.body();
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String,Object> map = mapper.readValue(playload, Map.class);
			
			double aj = (double) map.get("geographicalWidth");

			
			TypeOfApartment type = (map.get("typeOfApartment").equals("Soba")) ? TypeOfApartment.ROOM : TypeOfApartment.FULL_APARTMENT;
			
			Address address = new Address((String) map.get("street"),Integer.parseInt((String) map.get("numberHouse")),(String) map.get("city"),Integer.parseInt((String) map.get("postNumber")));	
			address.setCountry((String) map.get("country"));

			
			Location location = new Location((Double) map.get("geographicalWidth"),(Double) map.get("geographicalLength"),address);
			
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			Host h =  userDto.findeHost(user);
			
			
			
			Apartment a= new Apartment(type, Integer.parseInt((String) map.get("numberOfRoom")), Integer.parseInt((String) map.get("numberOfGuests")), location, h.getUserName(), Integer.parseInt((String) map.get("pricePerNight")), new Date(),new Date(), StatusApartment.PASSIVE);
			
			a.setId(appartmentDto.getAppartment().size()+1);
			
			int i=0;
			for (Integer s :(ArrayList<Integer>) map.get("content")) {
				
				a.getIdContetn().add(s);
			}
			
			appartmentDto.getAppartment().add(a);
			
			
			h.getIdApartment().add(a.getId());
			
			
			
			appartmentDto.saveFile();
			userDto.saveFile();
		
			return a.getId();
		});
		
		
		post("/getApartment", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();			
			int id =  Integer.parseInt(request.queryParams("id"));
			
		
			Apartment apartment =  appartmentDto.getApartmentById(id);
			apartment.setHost((Host)userDto.getUserById(apartment.getIdHost()));
			
			apartment.setContent(new ArrayList<ContentOfApartment>());
			
			for(int i : apartment.getIdContetn()) {
				ContentOfApartment content = contentsOfApartmentDTO.getContentsOfApartment(i);
				if(content != null) {
					apartment.getContent().add(content);
				}
			}
			
			Session ss = request.session(true);
			User user = ss.attribute("user");
			
			
			apartment.setComments(new ArrayList<CommentForApartment>());
			
			if(user != null) {
				if(user.getTypeOfUser() == TypeOfUser.ADMINISTRATOR || user.getTypeOfUser() == TypeOfUser.HOST) {
					apartment.setComments(commentsDTO.getAllCommentOfApartmentbyIdApartment(apartment.getId()));	
				} else {
					
					for(CommentForApartment com : commentsDTO.getAllCommentOfApartmentbyIdApartment(apartment.getId())) {
						if(com.getStatus() == StatusOfComment.APPROVED) {
							apartment.getComments().add(com);
						}		
					}	
				}
			
			} else {
				for(CommentForApartment com : commentsDTO.getAllCommentOfApartmentbyIdApartment(apartment.getId())) {
					
					
					if(com.getStatus() == StatusOfComment.APPROVED) {
						apartment.getComments().add(com);
					}		
				}	
				
				
				
			}
				
			for(CommentForApartment com : apartment.getComments()) {
				com.setGuest((Guest) userDto.getUserById(com.getIdGuest()));
			}
			
			
			
			
			return g.toJson(apartment);
		});
		
		
		post("/getContentsOfApartment", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();
			return g.toJson(contentsOfApartmentDTO.getContentsOfApartment());
		});
		
		post("/deleteContentsOfApartment", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();
			int id =  Integer.parseInt(request.queryParams("id"));
			
			
			// jos izbrisati iz svih apartmana
			
			for(Apartment apartment : appartmentDto.getAppartment()) {
				if(apartment.getIdContetn().contains(id)) {
					apartment.getIdContetn().remove(new Integer(id));
					break;
				}
			}
			
			
			
			contentsOfApartmentDTO.deleteContentsOfApartmentById(id);
			return g.toJson(contentsOfApartmentDTO.getContentsOfApartment());	
		});
		
		post("/addContentsOfApartment", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();
			request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("static/data/profile"));
			String name = request.queryParams("newItemName");
			
			Part uploadedFile = request.raw().getPart("image");
			
			Path out = Paths.get("static/data/contentsOfApartment/" + name + ".jpg");

		    try(final java.io.InputStream in = uploadedFile.getInputStream()){

		    	OutputStream outStream = new FileOutputStream(out.toString());		 
		    	IOUtils.copy(in, outStream);
		    	
		    	outStream.close();
		    	uploadedFile.delete();
		    	in.close();
		    	
		    	
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
			
		    
			contentsOfApartmentDTO.addContentsOfApartment(name, "/data/contentsOfApartment/" + name + ".jpg");
			
			return g.toJson(contentsOfApartmentDTO.getContentsOfApartment());	
		});
		
		
		post("/createHost", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();
			
			String playload = request.body();
			Host host = g.fromJson(playload, Host.class);
			
			userDto.deleteUser(host.getUserName());
			userDto.addUser(host);
			userDto.saveFile();
			
			return g.toJson(userDto.getUsers());
		});
		
		post("/deleteApartment", (request, response) -> {
			int id = Integer.parseInt(request.queryParams("id"));
			appartmentDto.delete(id);
			return true;
			
		});
		
		post("/blockUser", (request, response) -> {
			String username = request.queryParams("username");
			userDto.getUserById(username).setIsBlocked(true);
			userDto.saveFile();
			
			Gson g = new Gson();
			return g.toJson(userDto.getUsers());
		});
		
		post("/unblockUser", (request, response) -> {
			String username = request.queryParams("username");
			userDto.getUserById(username).setIsBlocked(false);
			userDto.saveFile();
		
			Gson g = new Gson();
			return g.toJson(userDto.getUsers());
		});
		
		post("/approveApartment", (request, response) -> {
			System.out.println("Aktiviram...");
			int id = Integer.parseInt(request.queryParams("id"));
			appartmentDto.approveApartment(id);
			return true;
			
		});
		
		post("/createReservation", (request, response) -> {
			response.type("application/json");
			Gson g = new Gson();
			
			String playload = request.body();
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String,Object> map = mapper.readValue(playload, Map.class);
			
			
			//Apartment a = appartmentDto.getApartmentById((Integer) map.get("id"));
			
			int idApartment = (Integer) map.get("idApartment");
			
			String idGuest = (String)map.get("idGuest");
			
			int numberDays = Integer.parseInt((String) map.get("numberOfNights"));
		
			String message = (String)map.get("message");
			
			String startDate = (String)map.get("startTime");
			
			System.out.println(map.get("price"));
			
			double price = Double.parseDouble((map.get("price").toString()));
	
			Apartment a= appartmentDto.getApartmentById(idApartment);
		
			
			for (String s :(ArrayList<String>) map.get("busyDays")) {
			
				a.getBusyDays().add(s);
			
			}
			
			int idReservatino = reservationDto.getReservations().size() + 1;
			
			
			Reservation reservation = new Reservation(idReservatino, idApartment, idGuest, startDate, numberDays, message, price, StatusReservation.CREATE);
		
			
			
			reservationDto.add(reservation);
			
			reservationDto.saveFile();
			appartmentDto.saveFile();
			
			return g.toJson(a);
		});
		
		
		
		
		
		
		
		
		get("/validationlogin", (req, res) -> {
			res.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = req.session(true);
			User user = ss.attribute("user");
			
			if(user != null) {
				res.status(403);
				return res;
			}
			
			return true;
		});
		
		
		post("/changeApartment", (req, res) -> {
			res.type("application/json");
			Gson g = new Gson();
			
			String playload = req.body();
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String,Object> map = mapper.readValue(playload, Map.class);
			Apartment a = appartmentDto.getApartmentById((Integer) map.get("id"));
			
			
	
			Integer numberofGuest;
			Integer price;
			if(map.get("numberOfGuests")== (Object) a.getNumberOfGuests()) {
				numberofGuest =	(Integer) map.get("numberOfGuests");
				
			}else {
				numberofGuest = Integer.parseInt((String) map.get("numberOfGuests"));
			}
			
			try {
				price = Integer.parseInt((String) map.get("pricePerNight"));
				}
				catch(Exception e) {
					price =	(Integer) map.get("pricePerNight");
				}
			
			ArrayList<Integer> con = new ArrayList<Integer>();
			
			for (Integer s :(ArrayList<Integer>) map.get("idContetn")) {
				con.add(s);	
			}
			
			a.setPricePerNight(price);
			a.setNumberOfGuests(numberofGuest);
			a.setIdContetn(con);
			
			
			appartmentDto.saveFile();
			
			
			return true;
		});

		
		post("/getReservation", (request, response) -> {
			response.type("application/json");
			
			Gson g = new Gson();
			
			Session ss = request.session(true);
			User user = ss.attribute("user");
			
			if(user != null) {
				
				ArrayList<Reservation> ret = new ArrayList<Reservation>();

				
				if(user.getTypeOfUser() == TypeOfUser.ADMINISTRATOR) {
					for(Reservation reservation : reservationDto.getReservations()) {
						reservation.setGuest((Guest) (userDto.getUserById(reservation.getIdGuest())));
						reservation.setReservedApartment(appartmentDto.getApartmentById(reservation.getIdApartment()));
						reservation.setHost((Host) userDto.getUserById(reservation.getReservedApartment().getIdHost()));
					}
					
					return g.toJson(reservationDto.getReservations());
					
					
				} else if(user.getTypeOfUser() == TypeOfUser.HOST) {
					Host host = (Host) user;
					for(Reservation reservation : reservationDto.getReservations()) {
						for(int id : host.getIdApartment()) {
							if(reservation.getIdApartment() == id) {
								ret.add(reservation);
							}
							
						}
						
					}
					
					for(Reservation reservation : ret) {
						reservation.setGuest((Guest) (userDto.getUserById(reservation.getIdGuest())));
						reservation.setReservedApartment(appartmentDto.getApartmentById(reservation.getIdApartment()));
						reservation.setHost((Host) userDto.getUserById(reservation.getReservedApartment().getIdHost()));
					}
					
					return g.toJson(ret);

					
					
				} else if(user.getTypeOfUser() == TypeOfUser.GUEST) {
					for(Reservation reservation : reservationDto.getReservations()) {
						if(user.getUserName().equals(reservation.getIdGuest())) {
							reservation.setGuest((Guest) (userDto.getUserById(reservation.getIdGuest())));
							reservation.setReservedApartment(appartmentDto.getApartmentById(reservation.getIdApartment()));
							reservation.setHost((Host) userDto.getUserById(reservation.getReservedApartment().getIdHost()));
							ret.add(reservation);
						}
					}
					
					
					
					return g.toJson(ret);
					
					
					
				}
				
				
				
				
				
			}
			
			
				
			return true;
		});
		
		post("/saveComment", (req,res)->{
			res.type("application/json");
			Gson g = new Gson();
			
			String playload = req.body();
			CommentForApartment com = g.fromJson(playload, CommentForApartment.class);
			
			com.setStatus(StatusOfComment.ON_HOLD);
			//pokupi sve komentare koje imaju id apartmana
			
			Boolean check = commentsDTO.checkIfGuestPlaceAllreadyComments(com.getIdApartment(), com.getIdGuest());
			
			if(check) {
				res.status(400);
				return res;
			}
			
			commentsDTO.getComments().add(com);
			commentsDTO.saveFile();
			
			return true;
		});
		
		post("/approveComment", (request, response) -> {
			Gson g = new Gson();
			
			System.out.println("Odobrava komentar...");
			
			String playload = request.body();
			CommentForApartment com = g.fromJson(playload, CommentForApartment.class);
			

			
			for(CommentForApartment comment : commentsDTO.getComments()) {
				if((comment.getIdApartment() == com.getIdApartment()) && (comment.getIdGuest().equals(com.getIdGuest())) && (comment.getText().equals(com.getText()))) {
					comment.setStatus(StatusOfComment.APPROVED);
					commentsDTO.saveFile();
					System.out.println("Odobrio!");
					return true;
				}
			}
			
			
			
			return false;
		});
		
		post("/rejectedComment", (request, response) -> {
			Gson g = new Gson();
			
			System.out.println("Zabranjuje komentar...");
			
			String playload = request.body();
			CommentForApartment com = g.fromJson(playload, CommentForApartment.class);
			
			
			for(CommentForApartment comment : commentsDTO.getComments()) {
				if((comment.getIdApartment() == com.getIdApartment()) && (comment.getIdGuest().equals(com.getIdGuest())) && (comment.getText().equals(com.getText()))) {
					
					comment.setStatus(StatusOfComment.REJECTED);
					commentsDTO.saveFile();
					System.out.println("Zabranio!");

					return true;
				}
			}
			
			
			
			return false;
		});
		
		
		
		post("/changeReservationStatus", (request, response) -> {
			int id = Integer.parseInt(request.queryParams("id"));
			String status = request.queryParams("status");
			
			Reservation reservation = reservationDto.getReservationById(id);
			if(reservation == null) return false;
			
			if(status.equals("DECLINE")) {
				reservation.setStatusReseravation(StatusReservation.DECLINE);
				reservationDto.saveFile();
				return true;
			}
			
			if(status.equals("QUITED")) {
				reservation.setStatusReseravation(StatusReservation.QUITED);
				reservationDto.saveFile();

				return true;
			}
			
			if(status.equals("CREATE")) {
				reservation.setStatusReseravation(StatusReservation.CREATE);
				reservationDto.saveFile();

				return true;
			}
			
			if(status.equals("ACCEPTED")) {
				reservation.setStatusReseravation(StatusReservation.ACCEPTED);
				reservationDto.saveFile();

				return true;
			}
			

			if(status.equals("COMPLETED")) {
				reservation.setStatusReseravation(StatusReservation.COMPLETED);
				reservationDto.saveFile();

				return true;
			}
			

			
			
			return false;
		});
		
		
		post("/getHolidays", (request, response) ->{
			Gson g = new Gson();		
			return g.toJson(holidaysDTO.getHolidays());		
		});
		
		
	}
}
