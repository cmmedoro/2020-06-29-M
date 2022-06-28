package it.polito.tdp.imdb.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.imdb.model.Actor;
import it.polito.tdp.imdb.model.Adiacenza;
import it.polito.tdp.imdb.model.Director;
import it.polito.tdp.imdb.model.Movie;

public class ImdbDAO {
	
	public List<Actor> listAllActors(){
		String sql = "SELECT * FROM actors";
		List<Actor> result = new ArrayList<Actor>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Actor actor = new Actor(res.getInt("id"), res.getString("first_name"), res.getString("last_name"),
						res.getString("gender"));
				
				result.add(actor);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Movie> listAllMovies(){
		String sql = "SELECT * FROM movies";
		List<Movie> result = new ArrayList<Movie>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Movie movie = new Movie(res.getInt("id"), res.getString("name"), 
						res.getInt("year"), res.getDouble("rank"));
				
				result.add(movie);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public Map<Integer, Director> listAllDirectors(){
		String sql = "SELECT * FROM directors";
		Map<Integer, Director> result = new HashMap<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Director director = new Director(res.getInt("id"), res.getString("first_name"), res.getString("last_name"));
				result.put(res.getInt("id"), director);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Director> getDirectorYear(Integer year, Map<Integer, Director> idMap){
		String sql = "SELECT DISTINCT md.director_id AS id "
				+ "FROM movies_directors md, movies m "
				+ "WHERE m.year = ? AND md.movie_id = m.id";
		List<Director> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, year);
			ResultSet res = st.executeQuery();
			while (res.next()) {
				Director d = idMap.get(res.getInt("id"));
				if( d != null) {
					result.add(d);
				}
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Adiacenza> getArchi(Integer year, Map<Integer, Director> idMap){
		String sql = "SELECT m1.director_id AS md1, m2.director_id AS md2, COUNT(DISTINCT r1.actor_id) AS peso "
				+ "FROM movies_directors m1, movies_directors m2, movies mv1, movies mv2, roles r1, roles r2 "
				+ "WHERE m1.director_id > m2.director_id AND m1.movie_id = mv1.id AND m2.movie_id = mv2.id AND mv1.year = ? AND mv2.year = ? AND r1.movie_id = mv1.id AND r2.movie_id = mv2.id AND r1.actor_id = r2.actor_id AND (mv1.id = mv2.id || mv1.id <> mv2.id) "
				+ "GROUP BY m1.director_id, m2.director_id";
		List<Adiacenza> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, year);
			st.setInt(2, year);
			ResultSet res = st.executeQuery();
			while (res.next()) {
				Director d1 = idMap.get(res.getInt("md1"));
				Director d2 = idMap.get(res.getInt("md2"));
				if(d1 != null && d2 != null) {
					result.add(new Adiacenza(d1, d2, res.getInt("peso")));
				}
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
}
