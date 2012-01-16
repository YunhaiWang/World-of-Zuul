/**
 *  This class is the main class of the "World of Zuul" application. 
 *  "World of Zuul" is a very simple, text based adventure game.  Users 
 *  can walk around some scenery. That's all. It should really be extended 
 *  to make it more interesting!
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 * 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * @author  Rico de Feijter, Marcellino van Hecke
 * @version 1.0
 */
package sherlock.holmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Game 
{
    private Parser parser;
    private Room currentRoom;
    private ArrayList<Item> currentRoomItems;
    private ArrayList<Room> visitedRooms;
    
    private XMLParser roomsXML;
    private XMLParser itemsXML;
    
    private HashMap<Integer, Room> rooms;
    
    private Player player;
    private Conversation conversation;
        
    /**
     * Create the game and initialise its internal map.
     */
    public Game() throws Exception
    {
        parser = new Parser();

        player = new Player(); //Create player
        
        rooms = new HashMap<Integer, Room>();//Create room storage
        visitedRooms = new ArrayList<Room>();//Create visited rooms storage
        
        //Get XML content for the rooms
        roomsXML = new XMLParser();
        roomsXML.setFilename("../rooms.xml");
        roomsXML.runXMLConvert();
        
        //Store all room data
        createRooms();
        
        //Get XML content for the items
        itemsXML = new XMLParser();
        itemsXML.setFilename("../items.xml");
        itemsXML.runXMLConvert();
        
        createItems();
        
        currentRoomItems = new ArrayList<Item>();
        
        conversation = new Conversation();
    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms()
    {
        //Create all rooms from XML file and set all properties
        Map<Integer, HashMap<String, String>> parentMap = roomsXML.getXMLData();
        Iterator<Map.Entry<Integer, HashMap<String, String>>> parentXML = parentMap.entrySet().iterator();

        while (parentXML.hasNext())
        {
            Map.Entry<Integer, HashMap<String, String>> parentEntry = parentXML.next();

            Map<String, String> map = parentEntry.getValue();
            Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();

            while (entries.hasNext())
            {
                Map.Entry<String, String> entry = entries.next();

                //Create rom object if not already exists
                if(!rooms.containsKey(parentEntry.getKey()))
                {
                    rooms.put(parentEntry.getKey(), new Room());
                }

                if(entry.getKey().equals("room_building"))
                {
                    //Set building ID where room is located
                    rooms.get(parentEntry.getKey()).setBuildingID(Integer.parseInt(entry.getValue()));
                }else if(entry.getKey().equals("room_name"))
                {
                    //Set room name
                    rooms.get(parentEntry.getKey()).setRoomName(entry.getValue());
                }else if(entry.getKey().equals("description"))
                {
                    //Set description for room
                    rooms.get(parentEntry.getKey()).setDescription(entry.getValue());
                }else if(entry.getKey().equals("item_requirement"))
                {
                    //Set description for room
                    rooms.get(parentEntry.getKey()).setItemRequirement(Integer.parseInt(entry.getValue()));
                }
            }
        }
        
        //Create exits for all the created rooms
        Map<Integer, HashMap<String, String>> parentExitMap = roomsXML.getXMLData();
        Iterator<Map.Entry<Integer, HashMap<String, String>>> parentExitXML = parentExitMap.entrySet().iterator();

        while (parentExitXML.hasNext())
        {
            Map.Entry<Integer, HashMap<String, String>> parentEntry = parentExitXML.next();

            Map<String, String> map = parentEntry.getValue();
            Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();

            while (entries.hasNext())
            {
                Map.Entry<String, String> entry = entries.next();
                
                if(entry.getKey().equals("north") || entry.getKey().equals("east") || entry.getKey().equals("south") || entry.getKey().equals("west") || entry.getKey().equals("up") || entry.getKey().equals("down"))
                {
                    String direction = "";
                    
                    if(entry.getKey().equals("north"))
                    {
                        direction = "noord";
                    }else if(entry.getKey().equals("east"))
                    {
                        direction = "oost";
                    }else if(entry.getKey().equals("south"))
                    {
                        direction = "zuid";
                    }else if(entry.getKey().equals("west"))
                    {
                        direction = "west";
                    }else if(entry.getKey().equals("up"))
                    {
                        direction = "boven";
                    }else if(entry.getKey().equals("down"))
                    {
                        direction = "beneden";
                    }
                    
                    //Get room object to set as exit
                    Room exit = rooms.get(Integer.parseInt(entry.getValue()));
                    
                    //Set exits for a room
                    rooms.get(parentEntry.getKey()).setExit(direction, exit);
                }
            }
        }

        currentRoom = rooms.get(1); //Starts the game in the first room
    }
    
    /**
     * Create all items and set al properties
     */
    private void createItems()
    {
        //Create all items from XML file and set all properties
        Map<Integer, HashMap<String, String>> parentMap = itemsXML.getXMLData();
        Iterator<Map.Entry<Integer, HashMap<String, String>>> parentXML = parentMap.entrySet().iterator();
        
        int itemRoomID = 0;
        
        while (parentXML.hasNext())
        {
            Map.Entry<Integer, HashMap<String, String>> parentEntry = parentXML.next();

            Map<String, String> map = parentEntry.getValue();
            Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
            
            Item currentItem = new Item();
            
            while (entries.hasNext())
            {
                Map.Entry<String, String> entry = entries.next();
                
                //Create item object if not already exists
                if(currentItem.getItemID() != 0)
                {
                    currentItem.setItemID(parentEntry.getKey());
                }

                if(entry.getKey().equals("item_room"))
                {
                    //Set room ID where item is located
                    itemRoomID = Integer.parseInt(entry.getValue());
                }else if(entry.getKey().equals("item_name"))
                {
                    //Set item name
                    currentItem.setItemName(entry.getValue());
                }else if(entry.getKey().equals("item_value"))
                {
                    //Set item value
                    currentItem.setItemValue(Integer.parseInt(entry.getValue()));
                    
                    //Add item object to room
                    rooms.get(itemRoomID).setItem(currentItem);
                }
            }
        }
    }
    
    /**
     * Sets player name
     */
    public void setPlayerName()
    {  
        player.setPlayerName(conversation.askQuestionInput("Hoe heet jij?"));
    }

    /**
     *  Main play routine.  Loops until end of play.
     */
    public void play()
    {
        setPlayerName();
        printWelcome(player.getPlayerName());

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.
                
        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome(String playername)
    {
        System.out.println();
        System.out.println("Welkom " + playername + " bij Sherlock Holmes en de Moord in het Bordeel.");
        System.out.println();
        System.out.println("Het is buiten slecht weer(London natuurlijk) en je krijgt een bericht van een");
        System.out.println("koerier. Er is een moord gepleegd in het lokale bordeel om de hoek! Aan jou");
        System.out.println("als Sherlock Holmes, de detective, om deze moord op te lossen. Om te starten");
        System.out.println("moet je eerst naar het politie bureau toe gaan.");
        System.out.println();
        System.out.println("Type 'help' als je hulp nodig hebt.");
        System.out.println();
        System.out.println(currentRoom.getLongDescription());
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) 
    {
        if(command.isUnknown()) {
            System.out.println("Ik weet niet wat je bedoelt...");
            return false;
        }

        String commandWord = command.getCommandWord();
        if (commandWord.equals("help")) {
            printHelp();
        }
        else if (commandWord.equals("ga")) {
            goRoom(command);
        }else if (commandWord.equals("kijk")) {
            lookInRoom();
        }
        else if (commandWord.equals("pak")) {
            pickupItem(command);
        }
        else if (commandWord.equals("tas")) {
            showInventory();
        }
        else if (commandWord.equals("verwijder")) {
            askRemoveInventoryItem(command);
        }
        else if (commandWord.equals("uitgangen")) {
            showExits();
        }
        else if (commandWord.equals("stoppen")) {
            askQuit();
        }
        // else command not recognised.
        return false;
    }

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the 
     * command words.
     */
    private void printHelp() 
    {
        System.out.println("Je hebt om hulp gevraagd? En dat voor een detective?");
        System.out.println("Jij stelt ook niet veel voor als je hulp nodig hebt..");
        System.out.println();
        System.out.println("Je commando woorden zijn:");
        parser.showCommands();
    }

    /** 
     * Try to go to one direction. If there is an exit, enter the new
     * room, otherwise print an error message.
     */
    private void goRoom(Command command) 
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Waar naar toe?");
            return;
        }
        
        if(command.getSecondWord().equals("terug"))
        {
            Room lastRoom = null;
            
            if(visitedRooms.size() >= 1)
            {
                lastRoom = visitedRooms.get(visitedRooms.size()-1);
            }
            
            if(lastRoom == currentRoom || lastRoom == null)
            {
                System.out.println("Je kunt niet verder terug gaan!");
            }else
            {
                currentRoom = lastRoom;
                
                //Remove last room from ArrayList
                visitedRooms.remove(visitedRooms.size()-1);
                
                System.out.println(currentRoom.getLongDescription());
            }
            
        }else
        {
            String direction = command.getSecondWord();

            // Try to leave current room.
            Room nextRoom = currentRoom.getExit(direction);

            if (nextRoom == null) {
                System.out.println("Er is geen deur!");
            }
            else {
                visitedRooms.add(currentRoom); //Add current room to visited rooms
                currentRoom = nextRoom; //Make next room current room
                System.out.println(currentRoom.getLongDescription());
            }
        }
    }
    
    /**
     * Compare room and inventory items with each other.
     * When a room item compares with a inventory item
     * it will be removed from the currentRoomItems
     * ArrayList to prevent already picked up items
     * to be displayed when looking or when trying
     * to pick it up.
     */
    public void compareRoomInventoryItems()
    {
        int ArrayListID = 0;
        
        if(currentRoomItems.size() < 1)
        {
            currentRoomItems = currentRoom.getItems();
        }
        
        if(player.getInventorySize() > 0)
        {
            for (Iterator it = currentRoomItems.iterator(); it.hasNext();)
            {
                Item roomItem = (Item) it.next();

                    for (Iterator inventoryIterator = player.getInventory().iterator(); inventoryIterator.hasNext();) {
                        Item inventoryItem = (Item) inventoryIterator.next();

                        if(inventoryItem.getItemName().equals(roomItem.getItemName()))
                        {
                            it.remove(); //If room item compares with inventory item remove it
                        }
                    }
                ArrayListID++;
            }
            
        }else if(player.getInventorySize() == currentRoomItems.size())
        {
            currentRoomItems.clear();
        }
    }
    
    /**
     * Look for items in the current room
     */
    public void lookInRoom()
    {
        int itemNumber = 1;
        String printString = "";
        
        //Compare room and inventory items again
        compareRoomInventoryItems();
        
        for (Iterator it = currentRoomItems.iterator(); it.hasNext();)
        {
            Item roomItem = (Item) it.next();
            
            printString += itemNumber + ": " + roomItem.getItemName() + "\n";
            itemNumber++;
        }
        
        //Cut of last new line
        if(printString.length() > 0)
        {
            printString = printString.substring(0, printString.lastIndexOf("\n"));
        }else
        {
            printString = "Er zijn geen voorwerpen in deze kamer.";
        }
        
        System.out.println(printString);
    }
    
    /**
     * Pickup specific item
     * @param Command 
     */
    public void pickupItem(Command command)
    {
        //Check if the player has less than 4 items
        if(player.getInventorySize() < 4)
        {
            if(!command.hasSecondWord()) {
                // if there is no second word, we don't know what to pick up
                System.out.println("Wat oppakken?");
                return;
            }

            int itemNumber = Integer.parseInt(command.getSecondWord()) - 1;

            //Compare room and inventory items again
            compareRoomInventoryItems();
            
            if(currentRoomItems.size() > itemNumber)
            {
                //Get Item object
                Item pickupItem = currentRoomItems.get(itemNumber);

                //Add item to inventory
                player.addItem(pickupItem);

                System.out.println("Het voorwerp '" + pickupItem.getItemName() + "' zit nu in je tas.");

                //Compare room and inventory items again
                compareRoomInventoryItems();
            }else
            {
                System.out.println("Voorwerp bestaat niet!");
            }
        }else
        {
            System.out.println("Het is buiten slecht weer(London natuurlijk) en je krijgt een bericht van een");
            System.out.println("Je hebt ruimte voor maar 4 items! Wil je iets oppakken? Dan zul je toch echt");
            System.out.println("iets moeten laten vallen " + player.getPlayerName() + " !");
        }
    }
    
    /**
     * Show players inventory
     */
    public void showInventory()
    {
        if(player.getInventorySize() > 0)
        {
            String printString = "Je tas bevat de volgende voorwerpen" + "\n";

            int itemNumber = 1;

            for (Iterator it = player.getInventory().iterator(); it.hasNext();) {
                Item roomItem = (Item) it.next();

                printString += itemNumber + ": " + roomItem.getItemName() + "\n";

                itemNumber++;
            }

            //Cut of last new line
            printString = printString.substring(0, printString.lastIndexOf("\n"));

            System.out.println(printString);
        }else
        {
            System.out.println("Je tas is leeg!");
        }
    }
    
    /**
     * Aks if the player is sure he or she wants to remove the
     * inventory item in case there is a second word.
     * @param Command The inserted command
     */
    public void askRemoveInventoryItem(Command command)
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know what to remove
            System.out.println("Verwijder wat?");
            return;
        }
        
        int inventoryItem = Integer.parseInt(command.getSecondWord()) - 1;
        
        System.out.println(player.getInventorySize() + " <=" + Integer.parseInt(command.getSecondWord()));
        
        if(Integer.parseInt(command.getSecondWord()) <= player.getInventorySize())
        {
            checkRemoveInventoryItem(conversation.askQuestionInput("Weet je zeker dat je wilt " + player.getInventoryItem(inventoryItem).getItemName() + " verwijderen? ja/nee"), inventoryItem);
        }else
        {
            System.out.println("Item wat je wil verwijderen bestaat niet..");
        }
    }
    
    /**
     * 
     * @param String The players input answer
     * @param int The item ID from the item the player wants to remove 
     */
    public void checkRemoveInventoryItem(String answer, int itemID)
    {
        if(answer.equals("ja"))
        {
            System.out.println(player.getInventoryItem(itemID).getItemName() + " is verwijderd!");
            removeInventoryItem(itemID);
        }else if(answer.equals("nee"))
        {
            System.out.println("Wat jij wil..");
        }
    }
    
    /**
     * Remove the specified item from the players inventory
     * @param int The inventory item ID 
     */
    public void removeInventoryItem(int itemID)
    {
        Item removableItem = player.getInventoryItem(itemID);
        
        //When an item is dropped add item to current room
        currentRoomItems.add(removableItem);
        
        //Remove item from inventory
        player.removeItem(itemID);
        
        //Compare room and inventory items again
        compareRoomInventoryItems();
    }
    
    /**
     * Print all room exits
     */
    public void showExits()
    {
        System.out.println(currentRoom.getExits());
    }
    
    /**
     * Asks the user if he/she wants to quit the game
     */
    public void askQuit()
    {
        checkQuit(conversation.askQuestionInput("Weet je zeker dat je wilt stoppen? ja/nee"));
    }
    
    /**
     * Asks for user input again
     */
    public void askQuitAgain()
    {
        checkQuit(conversation.askInput());
    }
    
    /**
     * Check if user really wants to quit the game
     * @param String User input
     */
    private void checkQuit(String answer)
    {
        if(answer.equals("ja"))
        {
            quit();
        }else if(answer.equals("nee"))
        {
            System.out.println("Wat jij wil..");
        }else
        {
            System.out.println("Is het nou zo moeilijk om ja of nee te antwoorden?");
            askQuitAgain();
        }
    }
    
    /** 
     * Quit the game and print message for 5 seconds
     */
    private void quit()
    {
        try{
            System.out.println("Het lukte gewoon niet om het spel af te maken?");
            System.out.println("Geef maar toe! Bedankt voor het spelen en tot ziens.");

            Thread.sleep(5000);//sleep for 5 seconds
            System.exit(0);  // signal that we want to quit
        }
        catch(Exception error){
            System.err.println(error);
        }
    }
    
    public static void main(String[] args)throws Exception
    {
        try{
            Game game = new Game();
            game.play();
        }catch(Exception e){
            System.err.println("Er heeft zich een ernstige fout voorgedaan waardoor het spel niet meer functioneerd.");
        }
        
    }
    
}

