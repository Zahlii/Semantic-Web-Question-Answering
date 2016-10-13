package de.uni_mannheim.semantic.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.Function;

class Levenshtein extends Function {

    @Override
    protected void xFunc() throws SQLException {
        if (args() != 2) {
            throw new SQLException("Levenshtein(text1,text2): Invalid argument count. Requires 2, but found " + args());
        }

        String t1 = value_text(0);
        String t2 = value_text(1);
        result(computeLevenshteinDistance(t1,t2));
    }
    
    private static int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }                                                                            
                                                                                 
    public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {      
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        
                                                                                 
        for (int i = 0; i <= lhs.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= rhs.length(); j++)                                 
            distance[0][j] = j;                                                  
                                                                                 
        for (int i = 1; i <= lhs.length(); i++)                                 
            for (int j = 1; j <= rhs.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
                                                                                 
        return distance[lhs.length()][rhs.length()];                           
    }   
}

public class DBPedia_Terms {

	private static Connection _connection;
	
	static 
	{
		try {
			Class.forName("org.sqlite.JDBC");
			_connection = DriverManager.getConnection("jdbc:sqlite:DBPedia_Terms.db");
			Function.create(_connection, Levenshtein.class.getSimpleName(), new Levenshtein());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static void getClasses() throws SQLException {
		Statement statement = _connection.createStatement();
		ResultSet rs = statement.executeQuery("select Levenshtein(\"Google\",\"Yahoo!\")");
		if(rs.next()) {
			System.out.println(rs.getString(1));
		}
	}

}
