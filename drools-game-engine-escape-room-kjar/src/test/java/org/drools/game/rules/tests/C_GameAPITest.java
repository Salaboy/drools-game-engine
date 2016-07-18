
package org.drools.game.rules.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.drools.game.core.api.GameMessage;
import org.drools.game.core.api.GameSession;
import org.drools.game.core.CommandExecutorImpl;
import org.drools.game.core.BaseGameConfigurationImpl;
import org.drools.game.core.BasePlayerConfigurationImpl;
import org.drools.game.core.GameCallbackServiceImpl;
import org.drools.game.core.GameMessageServiceImpl;
import org.drools.game.model.impl.base.BasePlayerImpl;
import org.drools.game.escape.room.model.house.Door;
import org.drools.game.escape.room.model.house.House;
import org.drools.game.escape.room.model.house.Outside;
import org.drools.game.escape.room.model.house.Room;
import org.drools.game.escape.room.model.items.Chest;
import org.drools.game.model.api.Item;
import org.drools.game.escape.room.model.items.Key;
import org.drools.game.escape.room.model.items.LightBulb;
import org.drools.game.escape.room.model.items.LightSwitch;
import org.drools.game.escape.room.model.items.MagicStone;
import org.drools.game.core.GameSessionImpl;
import org.drools.game.core.api.GameCallbackService;
import org.drools.game.core.api.GameConfiguration;
import org.drools.game.core.api.GameMessageService;
import org.drools.game.core.api.PlayerConfiguration;

import org.drools.game.model.api.ItemContainer;
import org.drools.game.model.api.Player;
import org.drools.game.escape.room.model.items.PickableItem;
import org.drools.game.escape.room.model.items.ShineInTheDarkItem;
import org.drools.game.escape.room.cmds.ExploreDoorsCommand;
import org.drools.game.escape.room.cmds.ExploreRoomCommand;
import org.drools.game.escape.room.cmds.PickItemCommand;
import org.drools.game.escape.room.cmds.TurnOffTheLightsCommand;
import org.drools.game.escape.room.cmds.TurnOnTheLightsCommand;
import org.drools.game.escape.room.cmds.UseDoorCommand;
import org.drools.game.escape.room.cmds.WhereAmICommand;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author salaboy
 *
 */
@RunWith( Arquillian.class )
public class C_GameAPITest {

    @Deployment
    public static JavaArchive createDeployment() {

        JavaArchive jar = ShrinkWrap.create( JavaArchive.class )
                .addClass( GameSessionImpl.class )
                .addClass(CommandExecutorImpl.class )
                .addClass( GameMessageService.class )
                .addClass( GameMessageServiceImpl.class )
                .addClass( GameCallbackService.class )
                .addClass( GameCallbackServiceImpl.class )
                .addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
//        System.out.println( jar.toString( true ) );
        return jar;
    }

    @Inject
    private GameSession game;

    /*
     * Test that the main hookpoints for a game are kicking in
     * Hook points: 
     * - There is a house , then we can do something here.. some extra initializations
     * - There is a room, extra initialization per room
     * - Everything is ready for the player to start ("Wake up call")
     * - There is light, so you can see things...
     */
    @Test
    public void bootstrapGameTest() {
        Player player = new BasePlayerImpl( "salaboy" );

        House house = createHouse( "My Escape The Room House", player );

        List initFacts = new ArrayList();
        initFacts.add( house );
        for ( Room r : house.getRooms() ) {
            initFacts.add( r );
            for ( Item i : r.getItems() ) {
                initFacts.add( i );
            }
            for ( Door d : r.getDoors() ) {
                initFacts.add( d );
            }
        }
        // Bootstrap the Game with the constructed house for this player
        GameConfiguration gameConfiguration = new BaseGameConfigurationImpl( initFacts, "" );

        game.bootstrap( gameConfiguration );

        PlayerConfiguration playerConfiguration = new BasePlayerConfigurationImpl( null );

        game.join( player, playerConfiguration );

        List<GameMessage> messages = game.getAllMessages( player.getName() );
        messages.addAll( game.getAllMessages( "system" ) );

        assertEquals( 5, messages.size() );

        Set<String> messageTexts = messages.stream().map( m -> m.getText() ).collect( Collectors.toSet() );

        assertThat( messageTexts,
                Matchers.containsInAnyOrder( "There is a house(My Escape The Room House) in the world",
                        "There is a room(Room A) in the house",
                        "There is a room(Outside) in the house",
                        "Wake up! You are trapped in Room A! You need to escape!",
                        "There is light in the room!" ) );

        game.drop( player );
        
        game.destroy();
    }

    /*
     * This test shows how to execute actions agains the world by modifying our facts
     */
    @Test
    public void exploreRoomTest() {
        Player player = new BasePlayerImpl( "salaboy" );

        House house = createHouse( "My Escape The Room House", player );
        List initFacts = new ArrayList();
        initFacts.add( house );
        for ( Room r : house.getRooms() ) {
            initFacts.add( r );
            for ( Item i : r.getItems() ) {
                initFacts.add( i );
            }
            for ( Door d : r.getDoors() ) {
                initFacts.add( d );
            }
        }
        // Bootstrap the Game with the constructed house for this player
        GameConfiguration gameConfiguration = new BaseGameConfigurationImpl( initFacts,
                "" );

        game.bootstrap( gameConfiguration );

        PlayerConfiguration playerConfiguration = new BasePlayerConfigurationImpl( null );

        game.join( player, playerConfiguration );

        Room roomIn = game.execute( new WhereAmICommand( player ) );

        List<Item> itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );

        assertEquals( 3, itemsInRoom.size() );

        assertTrue( itemsInRoom.get( 0 ) instanceof LightSwitch );
        assertTrue( itemsInRoom.get( 1 ) instanceof LightBulb );
        assertTrue( itemsInRoom.get( 2 ) instanceof Chest );

        List<GameMessage> messages = game.getAllMessages( player.getName() );
        messages.addAll( game.getAllMessages( "system" ) );
        assertEquals( 7, messages.size() );

        Set<String> messageTexts = messages.stream().map( m -> m.getText() ).collect( Collectors.toSet() );

        assertThat( messageTexts,
                Matchers.containsInAnyOrder( "There is a house(My Escape The Room House) in the world",
                        "There is a room(Room A) in the house",
                        "There is a room(Outside) in the house",
                        "Wake up! You are trapped in Room A! You need to escape!",
                        "There is light in the room!",
                        "You are in Room A",
                        "3 Items available" ) );

        game.drop( player );
        game.destroy();
    }

    /*
     * This test shows what happens when we turn of the lights of the room.
     * Now after the lights are off, we can see objects that Shine In the Dark (implements ShineInTheDarkItem interface)
     */
    @Test
    public void turnOffTheLightsTest() {
        Player player = new BasePlayerImpl( "salaboy" );

        House house = createHouse( "My Escape The Room House", player );

        List initFacts = new ArrayList();
        initFacts.add( house );
        for ( Room r : house.getRooms() ) {
            initFacts.add( r );
            for ( Item i : r.getItems() ) {
                initFacts.add( i );
            }
            for ( Door d : r.getDoors() ) {
                initFacts.add( d );
            }
        }

        // Bootstrap the Game with the constructed house for this player
        GameConfiguration gameConfiguration = new BaseGameConfigurationImpl( initFacts,
                "" );

        game.bootstrap( gameConfiguration );

        PlayerConfiguration playerConfiguration = new BasePlayerConfigurationImpl( null );

        game.join( player, playerConfiguration );

        Room roomIn = game.execute( new WhereAmICommand( player ) );

        List<Item> itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );

        assertEquals( 3, itemsInRoom.size() );

        assertTrue( itemsInRoom.get( 0 ) instanceof LightSwitch );
        assertTrue( itemsInRoom.get( 1 ) instanceof LightBulb );
        assertTrue( itemsInRoom.get( 2 ) instanceof Chest );

        LightSwitch lightSwitch = ( LightSwitch ) itemsInRoom.get( 0 );

        game.execute( new TurnOffTheLightsCommand( player, lightSwitch ) );

        itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );

        assertEquals( 1, itemsInRoom.size() );
        assertTrue( itemsInRoom.get( 0 ) instanceof ShineInTheDarkItem );
        assertTrue( itemsInRoom.get( 0 ) instanceof MagicStone );

        game.execute( new TurnOnTheLightsCommand( player, lightSwitch ) );

        itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );
        assertEquals( 3, itemsInRoom.size() );

        List<GameMessage> messages = game.getAllMessages( player.getName() );
        messages.addAll( game.getAllMessages( "system" ) );
        assertEquals( 12, messages.size() );

        Set<String> messageTexts = messages.stream().map( m -> m.getText() ).collect( Collectors.toSet() );

        assertThat( messageTexts,
                Matchers.containsInAnyOrder( "There is a house(My Escape The Room House) in the world",
                        "There is a room(Room A) in the house",
                        "There is a room(Outside) in the house",
                        "Wake up! You are trapped in Room A! You need to escape!",
                        "There is light in the room!",
                        "You are in Room A",
                        "3 Items available",
                        "Lights Turned Off",
                        "1 Items available",
                        "Lights Turned On" ) );

        game.drop( player );
        game.destroy();
    }

    /*
     * This test shows how when the user picks a key from the Chest, the door labeled with 
     *  the same name as the key automatically unlocks, based on the rule that define that behavior
     */
    @Test
    public void exploreContainerAndOpenDoorTest() {
        Player player = new BasePlayerImpl( "salaboy" );

        House house = createHouse( "My Escape The Room House", player );
        List initFacts = new ArrayList();
        initFacts.add( house );
        for ( Room r : house.getRooms() ) {
            initFacts.add( r );
            for ( Item i : r.getItems() ) {
                initFacts.add( i );
            }
            for ( Door d : r.getDoors() ) {
                initFacts.add( d );
            }
        }
        // Bootstrap the Game with the constructed house for this player
        GameConfiguration gameConfiguration = new BaseGameConfigurationImpl( initFacts,
                "" );

        game.bootstrap( gameConfiguration );

        PlayerConfiguration playerConfiguration = new BasePlayerConfigurationImpl( null );

        game.join( player, playerConfiguration );

        Room roomIn = game.execute( new WhereAmICommand( player ) );

        List<Door> doors = roomIn.getDoors();
        assertEquals( 1, doors.size() );
        assertTrue( doors.get( 0 ).isLocked() );
        assertTrue( !doors.get( 0 ).isOpen() );

        List<Item> itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );

        assertEquals( 3, itemsInRoom.size() );

        assertTrue( itemsInRoom.get( 2 ) instanceof Chest );
        assertTrue( itemsInRoom.get( 2 ) instanceof ItemContainer );
        ItemContainer container = ( ItemContainer ) itemsInRoom.get( 2 );

        List<Item> itemsInChest = container.getItems();
        assertEquals( 1, itemsInChest.size() );
        assertTrue( itemsInChest.get( 0 ) instanceof Key );

        Key key = ( Key ) itemsInChest.get( 0 );

        assertEquals( 0, player.getInventory().getItems().size() );

        game.execute( new PickItemCommand( player, container, new PickableItem( key ) ) );

        assertEquals( 1, player.getInventory().getItems().size() );

        List<GameMessage> messages = game.getAllMessages( player.getName() );
        messages.addAll( game.getAllMessages( "system" ) );
        assertEquals( 12, messages.size() );

        Set<String> messageTexts = messages.stream().map( m -> m.getText() ).collect( Collectors.toSet() );

        assertThat( messageTexts,
                Matchers.containsInAnyOrder( "There is a house(My Escape The Room House) in the world",
                        "There is a room(Room A) in the house",
                        "There is a room(Outside) in the house",
                        "Wake up! You are trapped in Room A! You need to escape!",
                        "There is light in the room!",
                        "You are in Room A",
                        "3 Items available",
                        "Item Picked! PickableItem{pickable=Key{name=Door A}}",
                        "There is a new item in our inventory!Key{name=Door A}",
                        "Door 'Door A' Unlocked and Opened!" ) );

        assertTrue( !doors.get( 0 ).isLocked() );
        assertTrue( doors.get( 0 ).isOpen() );
        game.drop( player );
        game.destroy();
    }

    /*
     * This test Open the door and uses it to walk Outside and accomplish the game goal! 
     */
    @Test
    public void useOpenedDoorReachGoalTest() {
        Player player = new BasePlayerImpl( "salaboy" );

        House house = createHouse( "My Escape The Room House", player );
        List initFacts = new ArrayList();
        initFacts.add( house );
        for ( Room r : house.getRooms() ) {
            initFacts.add( r );
            for ( Item i : r.getItems() ) {
                initFacts.add( i );
            }
            for ( Door d : r.getDoors() ) {
                initFacts.add( d );
            }
        }
        // Bootstrap the Game with the constructed house for this player
        GameConfiguration gameConfiguration = new BaseGameConfigurationImpl( initFacts,
                "" );

        game.bootstrap( gameConfiguration );

        PlayerConfiguration playerConfiguration = new BasePlayerConfigurationImpl( null );

        game.join( player, playerConfiguration );

        Room roomIn = game.execute( new WhereAmICommand( player ) );
        assertEquals( "Room A", roomIn.getName() );
        List<Door> doors = game.execute( new ExploreDoorsCommand( player, roomIn ) );
        assertEquals( 1, doors.size() );
        assertTrue( doors.get( 0 ).isLocked() );
        assertTrue( !doors.get( 0 ).isOpen() );

        List<Item> itemsInRoom = game.execute( new ExploreRoomCommand( player, roomIn ) );

        assertEquals( 3, itemsInRoom.size() );

        assertTrue( itemsInRoom.get( 2 ) instanceof Chest );
        assertTrue( itemsInRoom.get( 2 ) instanceof ItemContainer );
        ItemContainer container = ( ItemContainer ) itemsInRoom.get( 2 );

        List<Item> itemsInChest = container.getItems();
        assertEquals( 1, itemsInChest.size() );
        assertTrue( itemsInChest.get( 0 ) instanceof Key );

        Key key = ( Key ) itemsInChest.get( 0 );

        assertEquals( 0, player.getInventory().getItems().size() );

        game.execute( new PickItemCommand( player, container, new PickableItem( key ) ) );

        assertEquals( 1, player.getInventory().getItems().size() );

        Door door = doors.get( 0 );
        assertTrue( !door.isLocked() );
        assertTrue( door.isOpen() );

        game.execute( new UseDoorCommand( player, roomIn, door ) );

        roomIn = game.execute( new WhereAmICommand( player ) );

        assertEquals( "Outside", roomIn.getName() );

        List<GameMessage> messages = game.getAllMessages( player.getName() );
        messages.addAll( game.getAllMessages( "system" ) );
        assertEquals( 18, messages.size() );
        Set<String> messageTexts = messages.stream().map( m -> m.getText() ).collect( Collectors.toSet() );

        assertThat( messageTexts,
                Matchers.containsInAnyOrder( "There is a house(My Escape The Room House) in the world",
                        "There is a room(Room A) in the house",
                        "There is a room(Outside) in the house",
                        "Wake up! You are trapped in Room A! You need to escape!",
                        "There is light in the room!",
                        "You are in Room A",
                        "3 Items available",
                        "There is a new item in our inventory!Key{name=Door A}",
                        "Door 'Door A' Unlocked and Opened!",
                        "Congrats! You manage to escape the Room!",
                        "You are in Outside",
                        "Player moved from  Room A to Outside",
                        "1 Door(s) unlocked and open",
                        "Item Picked! PickableItem{pickable=Key{name=Door A}}" ) );
        game.drop( player );
        game.destroy();
    }

    /* House With:
        * Room A:
        *  - 1 Door to Outside
        *  - 1 chest with a key
        *  - 1 LightSwitch
        *  - 1 LightBulb
        *  - 1 Magic Stone
        * Outside (Goal)
     */
    private House createHouse( String name, Player player ) {
        House house = new House( name );

        Room roomA = new Room( "Room A" );
        Door doorA = new Door( "Door A" );
        doorA.setLeadsTo( "Outside" );
        roomA.getDoors().add( doorA );
        Chest chestA = new Chest( "Chest A" );
        Key keyA = new Key( "Door A" );
        chestA.getItems().add( keyA );
        LightBulb lightBulbA = new LightBulb( "Room A LightBulb", true, false );
        LightSwitch lightSwitchA = new LightSwitch( "Room A LightSwitch", true );
        MagicStone magicStone = new MagicStone();
        roomA.getItems().add( chestA );
        roomA.getItems().add( lightBulbA );
        roomA.getItems().add( lightSwitchA );
        roomA.getItems().add( magicStone );
        // Adding Rooms to my House
        house.getRooms().add( roomA );
        house.getRooms().add( new Outside() );
        // Setting player to Room A
        roomA.getPeopleInTheRoom().add( player.getName() );
        return house;
    }
}
