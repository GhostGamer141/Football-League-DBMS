package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal; // For handling salary in BigDecimal

public class Database {
    // Connection details
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASSWORD = "C3a#palmavenuegm";

    // Method to establish and return a connection to the database
    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting to connect to the database...");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method to test the connection
    public static void testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Connection successful!");
            } else {
                System.out.println("Connection failed.");
            }
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    public static List<Map<String, Object>> getLeagueTable() {
        List<Map<String, Object>> leagueTable = new ArrayList<>();

        String query = """
            SELECT 
                t.Team_Name as team_name,
                ts.Matches_Played as played,
                ts.Wins as won,
                ts.Draws as drawn,
                ts.Losses as lost,
                ts.Points as points,
                ts.Goal_Difference as goal_difference,
                RANK() OVER (ORDER BY ts.Points DESC, ts.Goal_Difference DESC) as position
            FROM 
                team_standings ts
            INNER JOIN 
                teams t ON ts.Team_ID = t.Team_ID
            ORDER BY 
                ts.Points DESC,
                ts.Goal_Difference DESC,
                t.Team_Name ASC
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("team_name", rs.getString("team_name"));
                row.put("played", rs.getInt("played"));
                row.put("won", rs.getInt("won"));
                row.put("drawn", rs.getInt("drawn"));
                row.put("lost", rs.getInt("lost"));
                row.put("points", rs.getInt("points"));
                row.put("goal_difference", rs.getInt("goal_difference"));
                row.put("position", rs.getInt("position"));
                leagueTable.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving league table: " + e.getMessage());
            e.printStackTrace();
        }

        return leagueTable;
    }

    public static List<Map<String, Object>> getPlayerStatistics() {
        List<Map<String, Object>> playerStatistics = new ArrayList<>();

        String query = """
        SELECT 
            p.Name AS player_name,
            p.Nationality AS nationality,
            p.Jersey_Number AS jersey_number,
            p.Position AS position,
            ps.Goals_Scored AS goals_scored,
            ps.Assists AS assists,
            ps.Health_Status AS health_status,
            ps.Yellow_Cards AS yellow_cards,
            ps.Red_Cards AS red_cards,
            ps.Tackles AS tackles
        FROM 
            player_statistics ps
        INNER JOIN 
            player p ON ps.Player_ID = p.Player_ID
        ORDER BY 
            p.Name ASC
    """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("player_name", rs.getString("player_name"));
                row.put("nationality", rs.getString("nationality"));
                row.put("jersey_number", rs.getInt("jersey_number"));
                row.put("position", rs.getString("position"));
                row.put("goals_scored", rs.getInt("goals_scored"));
                row.put("assists", rs.getInt("assists"));
                row.put("health_status", rs.getString("health_status"));
                row.put("yellow_cards", rs.getInt("yellow_cards"));
                row.put("red_cards", rs.getInt("red_cards"));
                row.put("tackles", rs.getInt("tackles"));
                playerStatistics.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving player statistics: " + e.getMessage());
            e.printStackTrace();
        }

        return playerStatistics;
    }

    public static boolean addUser(String username, String password, String role) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0; // Return true if insert was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(String username, String password, String role) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            connection = getConnection();

            // SQL query to update user details
            String query = "UPDATE users SET password = ?, role = ? WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, password);
            preparedStatement.setString(2, role);
            preparedStatement.setString(3, username);

            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }

    public static boolean updateReferee(Referee referee) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            connection = getConnection();

            // SQL query to update referee details
            String query = "UPDATE referee SET Name = ?, Role = ?, Nationality = ?, DOB = ? WHERE Referee_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, referee.getName());
            preparedStatement.setString(2, referee.getRole());
            preparedStatement.setString(3, referee.getNationality());
            preparedStatement.setString(4, referee.getDob()); // Ensure DOB is in correct format
            preparedStatement.setInt(5, referee.getRefereeId());

            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }

    public static boolean addReferee(Referee referee) {
        Connection connection = null;
        boolean isAdded = false;

        try {
            connection = getConnection();

            // SQL query to insert new referee details
            String query = "INSERT INTO referee (Referee_ID, Name, Role, Nationality, DOB) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, referee.getRefereeId());
            preparedStatement.setString(2, referee.getName());
            preparedStatement.setString(3, referee.getRole());
            preparedStatement.setString(4, referee.getNationality());
            preparedStatement.setString(5, referee.getDob());

            int rowsAffected = preparedStatement.executeUpdate();
            isAdded = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isAdded;
    }

    public static boolean updateContract(Contract contract) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            connection = getConnection();

            // SQL query to update the contract details
            String query = "UPDATE contract SET Team_ID = ?, Start_Date = ?, End_Date = ?, Personnel_Type = ?, Salary = ? WHERE Contract_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Set parameters based on the contract object
            if (contract.getTeamId() != null) {
                preparedStatement.setInt(1, contract.getTeamId());
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }

            preparedStatement.setString(2, contract.getStartDate());
            preparedStatement.setString(3, contract.getEndDate());
            preparedStatement.setString(4, contract.getPersonnelType());
            preparedStatement.setBigDecimal(5, contract.getSalary());
            preparedStatement.setInt(6, contract.getContractId());

            // Execute the update and check the result
            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }

    public static boolean updateTeamStaff(TeamStaff teamStaff) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            connection = getConnection();

            // SQL query to update team staff details
            String query = "UPDATE team_staff SET Team_ID = ?, Contract_ID = ?, Name = ?, Role = ?, Nationality = ?, DOB = ? WHERE Staff_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Set parameters based on the teamStaff object
            if (teamStaff.getTeamId() != null) {
                preparedStatement.setInt(1, teamStaff.getTeamId());
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }

            if (teamStaff.getContractId() != null) {
                preparedStatement.setInt(2, teamStaff.getContractId());
            } else {
                preparedStatement.setNull(2, java.sql.Types.INTEGER);
            }

            preparedStatement.setString(3, teamStaff.getName());
            preparedStatement.setString(4, teamStaff.getRole());
            preparedStatement.setString(5, teamStaff.getNationality());
            preparedStatement.setString(6, teamStaff.getDob());
            preparedStatement.setInt(7, teamStaff.getStaffId());

            // Execute the update and check the result
            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }

    public static boolean updatePlayer(Player player) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            connection = getConnection();

            // SQL query to update player details
            String query = "UPDATE player SET Name = ?, Contract_ID = ?, Nationality = ?, Jersey_Number = ?, Position = ?, DOB = ? WHERE Player_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, player.getName());
            preparedStatement.setInt(2, player.getContractId());
            preparedStatement.setString(3, player.getNationality());
            preparedStatement.setInt(4, player.getJerseyNumber());
            preparedStatement.setString(5, player.getPosition());
            preparedStatement.setString(6, player.getDob());
            preparedStatement.setInt(7, player.getPlayerId());

            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }

    public static boolean addPlayer(String name, Integer contractId, String nationality, Integer jerseyNumber, String position, String dob) {
        Connection connection = null;
        boolean isAdded = false;

        try {
            connection = getConnection();

            // SQL query to insert a new player
            String query = "INSERT INTO player (Name, Contract_ID, Nationality, Jersey_Number, Position, DOB) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, contractId); // can be null, ensure handling in DB
            preparedStatement.setString(3, nationality);
            preparedStatement.setInt(4, jerseyNumber);
            preparedStatement.setString(5, position);
            preparedStatement.setString(6, dob);

            int rowsAffected = preparedStatement.executeUpdate();
            isAdded = rowsAffected > 0;

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isAdded;
    }

    public static boolean updatePlayerStatistics(int playerId, int matchId, int goalsScored, int assists,
                                                 String healthStatus, int yellowCards, int redCards, int tackles) {
        Connection connection = null;
        boolean isUpdated = false;

        try {
            System.out.println("in db func");
            connection = getConnection();
            System.out.println("in update func");
            // SQL query to update player statistics
            String query = "UPDATE player_statistics SET Goals_Scored = ?, Assists = ?, Health_Status = ?, Yellow_Cards = ?, " +
                    "Red_Cards = ?, Tackles = ? WHERE Player_ID = ? AND Match_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, goalsScored);
            preparedStatement.setInt(2, assists);
            preparedStatement.setString(3, healthStatus);
            preparedStatement.setInt(4, yellowCards);
            preparedStatement.setInt(5, redCards);
            preparedStatement.setInt(6, tackles);
            preparedStatement.setInt(7, playerId);
            preparedStatement.setInt(8, matchId);

            int rowsAffected = preparedStatement.executeUpdate();
            isUpdated = rowsAffected > 0;
            System.out.println("updated player_credentials");
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Log the exception to console or file
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isUpdated;
    }




    public static class TeamStaff {
        private int staffId;
        private Integer teamId;
        private Integer contractId;
        private String name;
        private String role;
        private String nationality;
        private String dob;

        // Getters and Setters

        public int getStaffId() {
            return staffId;
        }

        public void setStaffId(int staffId) {
            this.staffId = staffId;
        }

        public Integer getTeamId() {
            return teamId;
        }

        public void setTeamId(Integer teamId) {
            this.teamId = teamId;
        }

        public Integer getContractId() {
            return contractId;
        }

        public void setContractId(Integer contractId) {
            this.contractId = contractId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
    }


    // Referee class updated for completeness
    public static class Referee {
        private int refereeId;
        private String name;
        private String role;
        private String nationality;
        private String dob;

        // Default constructor
        public Referee() {}

        // Constructor with all fields
        public Referee(int refereeId, String name, String role, String nationality, String dob) {
            this.refereeId = refereeId;
            this.name = name;
            this.role = role;
            this.nationality = nationality;
            this.dob = dob;
        }

        // Getters and setters for each field
        public int getRefereeId() {
            return refereeId;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getNationality() {
            return nationality;
        }

        public String getDob() {
            return dob;
        }

        public void setRefereeId(int refereeId) {
            this.refereeId = refereeId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
    }

    public static class Contract {
        private int contractId;
        private Integer teamId; // Nullable
        private String startDate;
        private String endDate;
        private String personnelType;
        private BigDecimal salary;

        // Getters and Setters
        public int getContractId() {
            return contractId;
        }
        public void setContractId(int contractId) {
            this.contractId = contractId;
        }
        public Integer getTeamId() {
            return teamId;
        }
        public void setTeamId(Integer teamId) {
            this.teamId = teamId;
        }
        public String getStartDate() {
            return startDate;
        }
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        public String getEndDate() {
            return endDate;
        }
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        public String getPersonnelType() {
            return personnelType;
        }
        public void setPersonnelType(String personnelType) {
            this.personnelType = personnelType;
        }
        public BigDecimal getSalary() {
            return salary;
        }
        public void setSalary(BigDecimal salary) {
            this.salary = salary;
        }
    }

    public static class Player {
        private int playerId;
        private String name;
        private Integer contractId;
        private String nationality;
        private Integer jerseyNumber;
        private String position;
        private String dob;

        // Getters and Setters

        public int getPlayerId() {
            return playerId;
        }

        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getContractId() {
            return contractId;
        }

        public void setContractId(Integer contractId) {
            this.contractId = contractId;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public Integer getJerseyNumber() {
            return jerseyNumber;
        }

        public void setJerseyNumber(Integer jerseyNumber) {
            this.jerseyNumber = jerseyNumber;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
    }















}