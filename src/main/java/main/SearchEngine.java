package main;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class SearchEngine {
	
		static public Connection con;
		static final String[] columns = new String[] {"id", "fileId", "word", "location"};
		static final String[] dataType = new String[] {"integer", "integer", "varchar(50)", "integer"};
		static final String tableName = "theWords";
		static ArrayList<String> validIds;
		
		//Create word index table and get connection
		public static void onStart() {
			try {
				FileDatabase.initialize(tableName, columns, dataType);
				con = FileDatabase.con;
				validateFileIds();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//main access
		public static ArrayList<String> search(int searchType, String query) {		
			ArrayList<String> returnArray = new ArrayList<String>();
			ArrayList<String> searchResults = new ArrayList<String>();
			final int and = 1, or = 2, exact = 3;
			
			validateFileIds();
			ArrayList<String> queryList = parseQuery(query);
			
			switch(searchType) {		
				case and: searchResults = andSearch(queryList);
					break;
				case or: searchResults = orSearch(queryList);
					break;		
				case exact: searchResults = exactSearch(queryList);
					break;
			}
			returnArray= validIdCheck(searchResults);
			return returnArray;
		}
		
		//
		private static ArrayList<String> orSearch(ArrayList<String> query) {
			ArrayList<String> returnArray = query;	
			return returnArray;
		}
		
		// Method written by Robert (Alex) Sapngler
				private static ArrayList<String> andSearch(ArrayList<String> query) {
		                        //ArrayList to store and searched files.
					ArrayList<String> andSrchArray = new ArrayList<String>();
		                                                
		                        // match valid file id's to the queried string.
		                        for (String s : query){
		                            try{
		                                // sql statemet for index
		                                String index = "SELECT DISTINCT fileId FROM "+ tableName +" WHERE word ='"+ s +"'";
		                                System.out.println(index);
		                                //create statement for con
		                                Statement state = con.createStatement();

		                                // create result statement 
		                                ResultSet result = state.executeQuery(index);

		                                // ArrayList to convert ResultSet to String
		                                ArrayList<String> resArray = new ArrayList<>();
		                                
		                               // check search to see if its empty or not.
		                               if(!result.next()){
		                                   // returns ",." if no records are found and breaks out of loop.
		                                   andSrchArray.add(",.");
		                                   break;
		                               }
		                               // checks if andSrchArray is empty   
		                               if(andSrchArray.isEmpty()){
		                                   while(result.next()){
		                                        andSrchArray.add(result.getString("fileId"));
		                                    }
		                               } else {
		                                   while(result.next()){
		                                       resArray.add(result.getString("fileId"));
		                                   }
		                               
		                               
		                                   for (int x=0 ; x < resArray.size() ;++x) {
		                                    	if(andSrchArray.contains(resArray.get(x))) {
		                                    		andSrchArray.add(resArray.get(x));
		                                    	}
		                                   }
		                               }                                                                    
		                            }catch(SQLException ex){
		                                System.out.println(ex);
		                                
		                            }
		                        }    
				        return andSrchArray;
				}
		
		//
		private static ArrayList<String> exactSearch(ArrayList<String> query) {
			ArrayList<String> returnArray = query;	
			ArrayList<String> firstArray = new ArrayList<String>();
			
			try {
	            String sql1 = "SELECT DISTINCT fileId, location FROM "+ tableName +" WHERE word ='"+ query.get(0) +"'";
	            Statement state1;
				state1 = con.createStatement();
				ResultSet result1 = state1.executeQuery(sql1);
            
				if(!result1.next()){
					returnArray.add(",.");
					return returnArray;     
				} else {
					while(result1.next()){
						firstArray.add(result1.getString("fileId"));
					}
				}
				for (int x = 1; x < query.size(); x++){
            	
					ArrayList<String> resultArray = new ArrayList<String>();
					// sql 
					String sql2 = "SELECT DISTINCT fileId FROM "+ tableName +" WHERE word ='"+ query.get(x) +"'";
					Statement state2;
					state2 = con.createStatement();
					ResultSet result2 = state2.executeQuery(sql2);
				
					if(!result2.next()){
						returnArray.add(",.");
						return returnArray;   
					}  
					else {
						resultArray.add(result2.getString("fileId"));
					}
					
					for(int y = 0; y < firstArray.size(); ++y){                 
						if(!resultArray.contains(firstArray.get(y))) {
							firstArray.remove(y);       
							--y;							
						}
					}                 
				}  
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Ready return array
			if(firstArray.equals(null)) {
				returnArray.add(",.");
			}
			else {
				for(int y = 0; y < firstArray.size(); ++y){
					returnArray.add(firstArray.get(y));
				}
			}
			return returnArray;
		}
		
		private static void indexFile(String id) {
			try {
				String word;
				int location = 0;
				
				//get file and set scanner
				String file[] = FileDatabase.getRow(Integer.parseInt(id));
				FileInputStream fileInput = new FileInputStream(file[1]);
				Scanner src  = new Scanner(fileInput);
				
				//index a file
				while(src.hasNext()) {
					word = formatWord(src.next());
					//index a word
					String sql = "INSERT INTO "+ tableName + " (fileId, word, location) VALUES('" + id + "', '" + word + "', '" + ++location + "')";
					System.out.println(sql);
					Statement state = con.createStatement();
					state.execute(sql);
				}
				src.close();
			}
			catch(SQLException | FileNotFoundException e){
				
			}
		}
		
		//removes all words from index for the file id
		private static void removeIndex(String id) {
			try {
				String sql = "DELETE FROM " + tableName + " WHERE fileId = " + id;
				Statement state = con.createStatement();
				state.execute(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//Takes the String from the search bar and breaks it into smaller strings
		private static ArrayList<String> parseQuery(String query) {
			ArrayList<String> queryList = new ArrayList<String>();
			
			Scanner src  = new Scanner(query);				
				while(src.hasNext() != false) {
					String word = formatWord(src.next());
					queryList.add(word);
				}
				
			src.close();
			
			//--------------------------------
			for(int x = 0; x < queryList.size(); ++x) {
				System.out.println("in query "+ queryList.get(x));
			}
			//--------------------------------

			return queryList;
		}
		
		//makes everything lower case for more accurate searches
		private static String formatWord(String word) {
			String returnString = word.toLowerCase();
			return returnString;
		}

		// returns list of id's to valid files
		private static void validateFileIds(){
			validIds = new ArrayList<String>();
			String[][] fileInfo;
			
			/*If the Search result turns up nothing its suppose to return ,.
				Added to validIds so it doesn't take it out if it see's it. */
			validIds.add(",.");	
			
			try {
				fileInfo = FileDatabase.getDatabase() ;			
				
				//checking to see if the files still there
				for(int x = 0 ; x < fileInfo.length ; ++x) {				
					try(FileInputStream file = new FileInputStream(fileInfo[x][1])) {
					} catch (IOException e) {
						continue;
					}
					//make sure index is accurate and up to date
					validateIndex(fileInfo[x][0]);	
					validIds.add(fileInfo[x][0]);			
				}	
			} catch (SQLException e) {
				e.printStackTrace();
			}					
			//--------------------------------
			for(int x = 0; x < validIds.size(); ++x) {
				System.out.println(validIds.get(x) + " Is Valid ID" );
			}
			//--------------------------------
		}
		
		//check that all Id's are valid
		private static ArrayList<String> validIdCheck(ArrayList<String> oneArray){
			ArrayList<String> returnArray =new ArrayList<String>();

			for (int x=0 ; x < oneArray.size() ;++x) {
				if(validIds.contains(oneArray.get(x))) {
					returnArray.add(oneArray.get(x));
				}
			}
			
			return returnArray;
		}

		//checks if files need to be reindexed
		private static void validateIndex(String id) {
			try {			
				String[] fileInfo = FileDatabase.getRow(Integer.parseInt(id));
				
				//collect dates
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				File file = new File(fileInfo[1]);
				String dateModified  = dateFormat.format(file.lastModified()); 		
				String dateIndexed = null;
				
				if(!(fileInfo[2] == null)) {
					dateIndexed = fileInfo[2];
				}
				
				//if dateIndexed is after dateModified
				if(dateIndexed == null || dateIndexed.compareTo(dateModified) < 0) {
					//reindex file, clear from index and then index again
					removeIndex(fileInfo[0]);
					indexFile(fileInfo[0]);
					
					//update date indexed, this might need to go somewhere else but i made it before i thought about it.
					//get current time
					String currentTime  = dateFormat.format(Calendar.getInstance().getTime());
					
					//UPDATE thePile SET date = 'current time' WHERE id = id
					String sql = "UPDATE " + FileDatabase.tableName + " SET " + FileDatabase.columns[2] + " = '" 
							+ currentTime +"' WHERE " + FileDatabase.columns[0] + "= "+ id;
					System.out.println(sql);
					Statement state = con.createStatement();
					state.execute(sql);
				}
				
			} catch (IllegalArgumentException | SQLException e) {
				e.printStackTrace();
			}
			return;
		}

}
