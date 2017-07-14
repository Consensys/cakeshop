/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.dao;

import com.jpmorgan.cakeshop.model.User;
import org.hibernate.Session;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDAO extends BaseDAO {

    private final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Transactional
    public User getUser(String userName) {
        if (null != getCurrentSession()) {
            return getCurrentSession().get(User.class, userName);
        }
        return null;
    }

    @Transactional
    public Boolean save(User user) {
        if (null != getCurrentSession()) {
            if (null != getCurrentSession().get(User.class, user.getUserName())) {
                return false;
            } else {
                user.setPassword(ENCODER.encode(user.getPassword()));
                getCurrentSession().save(user);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Boolean authenticate(String userName, String password) {
        if (null != getCurrentSession()) {
            User user = getCurrentSession().get(User.class, userName);
            if (ENCODER.matches(password, user.getPassword())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void resetPassword(String userName, String newPassword) {
        if (null != getCurrentSession()) {
            User user = getCurrentSession().get(User.class, userName);
            if (null != user) {
                user.setPassword(ENCODER.encode(newPassword));
                getCurrentSession().update(user);
            }
        }
    }

    @Override
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE USERS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
