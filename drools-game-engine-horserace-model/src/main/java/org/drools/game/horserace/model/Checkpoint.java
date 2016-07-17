/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.game.horserace.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel
 */
public class Checkpoint {

    private String id;
    private int order;
    private boolean isStartFinish;
    private List<String> players = new ArrayList<>();

    private Checkpoint() {
        
    }

    public Checkpoint( String id , int order, boolean isStartFinish) {
        this();
        this.id = id;
        this.order = order;
        this.isStartFinish = isStartFinish;
    }
    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }
    
    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers( List<String> players ) {
        this.players = players;
    }

    public void addPlayer( String player ) {
        this.players.add( player );
    }

    public void removePlayer( String player ) {
        this.players.remove( player );
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public boolean isIsStartFinish()
    {
        return isStartFinish;
    }

    public void setIsStartFinish(boolean isStartFinish)
    {
        this.isStartFinish = isStartFinish;
    }
    
    

    @Override
    public String toString() {
        return "Room{" + "id=" + id + "}";
    }
    
    

}
