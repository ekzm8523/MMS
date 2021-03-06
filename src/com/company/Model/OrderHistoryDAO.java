package com.company.Model;

import java.sql.*;
import java.util.ArrayList;

public class OrderHistoryDAO {

    Connection con;
    PreparedStatement pstmt;
    Statement stmt;
    ResultSet rs;
    String jdbcDriver = "com.mysql.cj.jdbc.Driver";
    String jdbcUrl = "jdbc:mysql://mms.crgsa3qt3jqa.ap-northeast-2.rds.amazonaws.com/mms?user=jaewon&password=wlfkf132";
    String userid = "jaewon";
    String pwd = "wlfkf132";
    String sql;

    public OrderHistoryDAO() {

    }

    // DB 연결 함수
    public void connectDB() {
        try {
            Class.forName(jdbcDriver);
            System.out.println("드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("데이터베이스 연결 준비...");
            con = DriverManager.getConnection(jdbcUrl, userid, pwd);
            System.out.println("데이터베이스 연결 성공");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    // DB 종료 함수
    public void closeDB() {
        try {
            pstmt.close();
            if(rs != null) rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // OrderHistory 테이블에 있는 모든 행 반환
    public ArrayList<OrderHistoryDTO> getAll() {
        connectDB();
        sql = "select * from OrderHistory";

        ArrayList<OrderHistoryDTO> datas = new ArrayList<OrderHistoryDTO>();

        try {
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while(rs.next()) {
                OrderHistoryDTO ordHis = new OrderHistoryDTO();
                ordHis.setHistory_id(rs.getInt("history_id"));
                ordHis.setOrder_code(rs.getInt("order_code"));
                ordHis.setPr_code(rs.getInt("pr_code"));
                ordHis.setPr_name(rs.getString("pr_name"));
                ordHis.setPr_count(rs.getInt("pr_count"));
                ordHis.setPr_price(rs.getInt("pr_price"));
                datas.add(ordHis);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        closeDB();

        if(datas.isEmpty()) return null;
        else return datas;

    }

    // OrderHistory 테이블에서 인자로 받은 historyId에 해당하는 행 검색 후 반환
    public OrderHistoryDTO getOrderHistory(int historyId) {
        connectDB();
        sql = "select * from OrderHistory where history_id = ?";
        OrderHistoryDTO ordHis = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, historyId);
            rs = pstmt.executeQuery();
            rs.next();
            ordHis = new OrderHistoryDTO();
            ordHis.setHistory_id(rs.getInt("history_id"));
            ordHis.setOrder_code(rs.getInt("order_code"));
            ordHis.setPr_code(rs.getInt("pr_code"));
            ordHis.setPr_name(rs.getString("pr_name"));
            ordHis.setPr_count(rs.getInt("pr_count"));
            ordHis.setPr_price(rs.getInt("pr_price"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        closeDB();

        return ordHis;
    }

    // OrderHistory테이블에 새로운 내역 추가 --> 서버에서 처리하므로 사용안한 함수
    public boolean addOrderHistory(OrderHistoryDTO ordHis) {
        connectDB();
        sql = "insert into OrderHistory(order_code, pr_code, pr_name, pr_count, pr_price) value(?, ?, ?, ?, ?)";

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, ordHis.getOrder_code());
            pstmt.setInt(2, ordHis.getPr_code());
            pstmt.setString(3, ordHis.getPr_name());
            pstmt.setInt(4, ordHis.getPr_count());
            pstmt.setInt(5, ordHis.getPr_price());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        closeDB();

        return true;
    }

}
