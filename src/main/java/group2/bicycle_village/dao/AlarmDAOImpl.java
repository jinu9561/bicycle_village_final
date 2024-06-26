package group2.bicycle_village.dao;

import group2.bicycle_village.common.dto.AlarmDTO;
import group2.bicycle_village.common.dto.BoardDTO;
import group2.bicycle_village.common.dto.UserDTO;
import group2.bicycle_village.common.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlarmDAOImpl implements AlarmDAO {
    @Override
    public int insertAlarm(AlarmDTO alarm) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        String sql = "insert into alarm values(alarm_seq.nextval, ?, ?, ?, ?)";
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, alarm.getUserSeq());
            ps.setString(2, alarm.getAlarmContent());
            ps.setInt(3,alarm.getIsSeen());
            ps.setString(4, alarm.getLinkURL());
            result = ps.executeUpdate();
        } finally {
            DbUtil.close(con, ps, null);
        }
        return result;
    }

    @Override
    public List<AlarmDTO> selectAllAlarm(String id) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT alarm_seq, alarm_content, link_url FROM alarm " +
                "WHERE user_seq in (SELECT user_seq FROM member WHERE user_id = ?) " +
                "ORDER BY alarm_seq DESC";
        List<AlarmDTO> alarmList = new ArrayList<AlarmDTO>();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                //알림 내용 읽어오기
                AlarmDTO alarm = new AlarmDTO(rs.getString("alarm_content"), 0);
                alarm.setAlarmSeq(rs.getLong("alarm_seq"));
                alarm.setLinkURL(rs.getString("link_url"));
                alarmList.add(alarm);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        return alarmList;
    }

    @Override
    public int updateAlarm(String id, long alarmSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        String sql = "update alarm set is_seen = 1 " +
                "where user_seq in (select user_seq from member where user_id = ?) " +
                "and alarm_seq = ?";
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ps.setLong(2, alarmSeq);
            result = ps.executeUpdate();
        } finally {
            DbUtil.close(con, ps, null);
        }
        return result;
    }

    @Override
    public String linked(long alarmSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select link_url from alarm where alarm_seq = ?";
        String url = null;
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, alarmSeq);
            rs = ps.executeQuery();
            if(rs.next()) {
                url = rs.getString(1);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        return url;
    }

    @Override
    public UserDTO userIdAndNickname(long userSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select distinct m.user_id, m.nickname from board b " +
                "join member m using(user_seq) where user_seq = ?";
        UserDTO user = null;
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, userSeq);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = new UserDTO(rs.getString(1), null, null,
                        rs.getString(2), null, null, null, null, 0);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        return user;
    }

    @Override
    public List<UserDTO> searchFollower(long userSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select * from member where user_seq in (select follower from follow where follow = ?)";
        List<UserDTO> userList = new ArrayList<>();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, userSeq);
            rs = ps.executeQuery();
            while (rs.next()) {
                UserDTO user = new UserDTO(rs.getInt("user_seq"), rs.getString("user_id"),
                        rs.getString("user_pwd"), rs.getString("user_name"),
                        rs.getString("nickname"), rs.getString("user_tel"),
                        rs.getString("birth"), rs.getString("user_email"),
                        rs.getString("user_gender"), rs.getInt("status"),
                        rs.getDate("reg_date"));
                userList.add(user);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        return userList;
    }

    @Override
    public List<UserDTO> searchDips(long boardSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM member WHERE user_seq IN (SELECT bookmark.user_seq FROM board " +
                "JOIN bookmark USING(board_seq) WHERE board_seq = ?)";
        List<UserDTO> userList = new ArrayList<>();
        System.out.println("AlarmDAOImpl searchDips start");
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, boardSeq);
            rs = ps.executeQuery();
            while (rs.next()) {
                UserDTO user = new UserDTO(rs.getInt("user_seq"), rs.getString("user_id"),
                        rs.getString("user_pwd"), rs.getString("user_name"),
                        rs.getString("nickname"), rs.getString("user_tel"),
                        rs.getString("birth"), rs.getString("user_email"),
                        rs.getString("user_gender"), rs.getInt("status"),
                        rs.getDate("reg_date"));
                userList.add(user);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        System.out.println("AlarmDAOImpl searchDips end");
        return userList;
    }

    @Override
    public long searchBoardSeq(long userSeq) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT board_seq FROM board " +
                "WHERE reg_date = (SELECT MAX(reg_date) FROM board WHERE user_seq = ?)";
        long boardSeq = 0;
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, (int) userSeq);
            rs = ps.executeQuery();
            if (rs.next()) {
                boardSeq = rs.getLong(1);
            }
        } finally {
            DbUtil.close(con, ps, rs);
        }
        return boardSeq;
    }

    @Override
    public int setLinkURL(String url) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        String sql = "UPDATE alarm SET link_url=? WHERE link_url IS NULL";
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, url);
            result = ps.executeUpdate();
        } finally {
            DbUtil.close(con,ps,null);
        }
        return result;
    }

    @Override
    public List<Integer> alarmCheck(String id) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT is_seen FROM alarm " +
                "WHERE user_seq = (SELECT user_seq FROM member WHERE user_id = ?)";
        List<Integer> list = new ArrayList<Integer>();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1,id);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        } finally {
            DbUtil.close(con,ps,rs);
        }
        return list;
    }
}
