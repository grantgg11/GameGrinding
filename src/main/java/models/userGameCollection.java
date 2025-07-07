package models;

import java.util.List;

/**
 * Represents a user's personal game collection.
 * Associates a user with a list of games they catalog.
 */
public class userGameCollection {
	
	private int collectionID;	// Unique identifier for the collection
	private user user;			// User associated with the collection
	private List<game> games;	// List of games in the collection
	
	/** Default constructor */
	public userGameCollection() {	}
	
	/**
	 * Constructor to initialize a new game collection with a user and a list of games.
	 *
	 * @param collectionID Unique ID for the collection.
	 * @param user         User associated with the collection.
	 * @param games        List of games in the collection.
	 */
	public userGameCollection(int collectionID, user user, List<game> games) {
		this.collectionID = collectionID;
		this.user = user;
		this.games = games;
	}
	
	/**
	 * Constructor to initialize a new game collection with a user and a list of games.
	 *
	 * @param user  User associated with the collection.
	 * @param games List of games in the collection.
	 */
	public userGameCollection( user user, List<game> games) {
		this.user = user;
		this.games = games;
	}

	//------------------ Getters and Setters ------------------
	
	public int getCollectionID() {
		return collectionID;
	}

	public void setCollectionID(int collectionID) {
		this.collectionID = collectionID;
	}

	public user getUser() {
		return user;
	}

	public void setUser(user user) {
		this.user = user;
	}

	public List<game> getGames() {
		return games;
	}

	public void setGames(List<game> games) {
		this.games = games;
	}
	
	
}
