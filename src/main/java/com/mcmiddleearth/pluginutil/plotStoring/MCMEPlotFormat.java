/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import com.mcmiddleearth.pluginutil.nms.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class MCMEPlotFormat implements PlotStorageFormat {
    
    //private static final String entityExt = ".emcme";
    
    /*
     * Binary format:
     * (number of Bytes: content)
     * 4: Version number
     * 4: <length> of world name bytes
     *    <length>: world name in UTF-8 charset
     * 4: Low Corner x
     * 4: Low Corner y
     * 4: Low Corner z
     * 4: Size x
     * 4: Size y
     * 4: Size z
     * 4: number of palette entries
     *     Palette entry:
     *     4:          <datalength> length of palette entry data
     *     <datalength>: blockData in UTF-8 charset
     * 4: number of biome palette entries
     *     4:          <datalength> length of biome palette entry data
     *     <datalength>: biome name in UTF-8 charset
     * All blocks in area, ordered by x,z,y:
     *     Before each x,z column:
     *     4: Index of biome palette entry of this column
     *     4: y coordinate of highest block in column
     *     For each in column:
     *     4: Index of palette entries for this block
     * All Tile Entities in area
     *     4: number of Tile Entities
     *     variable: nbt data of Tile Entities
     * Other Entities in area (Paintings, Item Frames, Armor Stands)
     *     4: number of Entities
     *     variable: nbt data of Entities
    */
    
    public static final int VERSION = 2;
    /* version changelog:
     * 2: Added world name after version
    */
    
    private Vector resultSize;

    public Vector getResultSize() {
        return resultSize;
    }

    @Override
    public void save(IStoragePlot plot, DataOutputStream out) throws IOException{
//Logger.getGlobal().info("Don't use this Async!!!");
            StoragePlotSnapshot snap = new StoragePlotSnapshot(plot);
            save(plot, out, snap);
    }
    
    @Override
    public void save(IStoragePlot plot, DataOutputStream out, StoragePlotSnapshot snap) throws IOException{
        try {
            out.writeInt(VERSION);
            byte[] worldNameBytes = plot.getWorld().getName().getBytes(StandardCharsets.UTF_8);
            out.writeInt(worldNameBytes.length);
            out.write(worldNameBytes);
            out.writeInt(plot.getLowCorner().getBlockX());
            out.writeInt(plot.getLowCorner().getBlockY());
            out.writeInt(plot.getLowCorner().getBlockZ());
            out.writeInt(plot.getHighCorner().getBlockX()-plot.getLowCorner().getBlockX()+1);
            out.writeInt(plot.getHighCorner().getBlockY()-plot.getLowCorner().getBlockY()+1);
            out.writeInt(plot.getHighCorner().getBlockZ()-plot.getLowCorner().getBlockZ()+1);
            Map<BlockData,Integer> paletteMap = new HashMap<>();
            List<BlockData> palette = new ArrayList<>();
            Map<Biome,Integer> biomePaletteMap = new HashMap<>();
            List<Biome> biomePalette = new ArrayList<>();
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                    Biome biome = snap.getBiome(x, z);
                    Integer biomePaletteIndex = biomePaletteMap.get(biome);
                    if(biomePaletteIndex==null) {
                        biomePaletteMap.put(biome, biomePalette.size());
                        biomePalette.add(biome);
                    }
                    for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY()
                                                              && y <= snap.getMaxY(x, z); ++y) {
                        BlockData blockData = snap.getBlockData(x, y, z);
                        Integer paletteIndex = paletteMap.get(blockData);
                        if(paletteIndex==null) {
                            paletteMap.put(blockData, palette.size());
                            palette.add(blockData);
                        }
                    }
                }
            }
            out.writeInt(palette.size()); //write length of palette
            for(int i=0; i<palette.size();i++) {
                String blockDataString = palette.get(i).getAsString();
                byte[] blockDataBytes = blockDataString.getBytes(StandardCharsets.UTF_8);
                out.writeInt(blockDataBytes.length); //write length of next blockdata
                out.write(blockDataBytes);
            }
            out.writeInt(biomePalette.size()); //write length of palette
            for(int i=0; i<biomePalette.size();i++) {
                String biomeDataString = biomePalette.get(i).getKey().getKey();
                byte[] biomeDataBytes = biomeDataString.getBytes(StandardCharsets.UTF_8);
                out.writeInt(biomeDataBytes.length); //write length of next blockdata
                out.write(biomeDataBytes);
            }
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                    int biomeIndex = biomePaletteMap.get(snap.getBiome(x, z));
//Logger.getGlobal().info("save biome: "+biomeIndex +" "+snap.getBiome(x,z)+"save maxY: "+snap.getMaxY(x,z));
                    out.writeInt(biomeIndex);
                    out.writeInt(snap.getMaxY(x, z));
                    for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY()
                                                              && y <= snap.getMaxY(x, z); ++y) {
                        BlockData blockData = snap.getBlockData(x, y, z);
                        out.writeInt(paletteMap.get(blockData));
                    }
                }
            }
            List tileEntities = new ArrayList();
            for(BlockState state: snap.getTileEntities()) {
                Object nbt = AccessCraftBukkit.getSnapshotNBT(state);
                tileEntities.add(nbt);
            }
            out.writeInt(tileEntities.size());
            for(Object nbt: tileEntities) {
                AccessNBT.writeNBTToStream(nbt, out);
            }
            Collection<Entity> entities = snap.getEntities();
            out.writeInt(entities.size());
            for(Entity entity: entities) {
                Object nbt = AccessNBT.createNBTCompound();
                Object nmsEntity = AccessCraftBukkit.getNMSEntity(entity);
                String entityType = (String) AccessWorld.getEntityType(nmsEntity);
Logger.getGlobal().info("Entity Description id: "+entityType);
                AccessNBT.setString(nbt, "id", entityType);
                nbt = AccessWorld.writeEntityNBT(nmsEntity, nbt);
                AccessNBT.writeNBTToStream(nbt, out);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public Vector readSize(DataInputStream in) throws IOException {
        in.readInt(); //Version
        int nameBytes = in.readInt();  //world name length
        in.read(new byte[nameBytes]); //world name
        in.readInt();in.readInt();in.readInt(); //Location
        return new Vector(in.readInt(),in.readInt(),in.readInt());
    }
    
    @Override
    public void load(DataInputStream in) throws IOException, InvalidRestoreDataException {
        load(null, null, in);
    }
    
    @Override
    public void load(final IStoragePlot plot, DataInputStream in) throws IOException, InvalidRestoreDataException {
        Location loc = new Location(plot.getWorld(),plot.getLowCorner().getBlockX(),
                                                    plot.getLowCorner().getBlockY(),
                                                    plot.getLowCorner().getBlockZ());
        Vector size = new Vector(plot.getHighCorner().getBlockX()-plot.getLowCorner().getBlockX()+1,
                                 plot.getHighCorner().getBlockY()-plot.getLowCorner().getBlockY()+1,
                                 plot.getHighCorner().getBlockZ()-plot.getLowCorner().getBlockZ()+1);
        load(loc, size, in);
    }               
    
    /**
     * Loads MCME Storage Plot data from file and places it at the specified location.
     * If specified size doesn't fit with size of storage data an IOException is thrown.
     * @param location Where to place the restore data may be null to use stored location
     * @param size Expected size of restore data, may be null to skip size test
     * @param in
     * @throws IOException 
     * @throws com.mcmiddleearth.pluginutil.plotStoring.InvalidRestoreDataException 
     */
    @Override
    public void load(final Location location, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
            load(location, 0 , size, in);
    }
    
    @Override
    public void load(Location loca, final int rotations, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        this.load(loca, rotations, new boolean[3], size, in);
    }
    
    @Override
    public void load(Location loca, final int rotations, boolean[] flip,
                     Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        load(loca, rotations, flip, true, true, size, in);
    }

    public void load(Location loca, final int rotations, boolean[] flip,
                     final boolean withAir, final boolean withBiome,
                     Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        load(loca, rotations,flip,withAir,withBiome,size, false, in);
    }

    public void load(Location loca, final int rotations, boolean[] flip,
    final boolean withAir, final boolean withBiome,
    Vector size, boolean legacyBlocks, DataInputStream in) throws IOException, InvalidRestoreDataException{
        //try {
        resultSize = null;
        int version = in.readInt();
        if(version!=VERSION) {
            throw new InvalidRestoreDataException("Invalid storage data version: "+version+" ("+VERSION+" expected)");
        }
        int worldNameDataLength = in.readInt();
        byte[] worldNameByteData = new byte[worldNameDataLength];
        in.readFully(worldNameByteData);
        String worldName =  new String(worldNameByteData, StandardCharsets.UTF_8);
        World originalWorld = Bukkit.getWorld(worldName);
        Location originalLoc;
        final Location location;
        final Vector shift;
        if(originalWorld!=null) {
            originalLoc = new Location(originalWorld,in.readInt(),in.readInt(),in.readInt());
        } else if(loca != null) {
            originalLoc = new Location(loca.getWorld(),in.readInt(),in.readInt(),in.readInt());
        } else {
            throw new InvalidRestoreDataException("Missing restore world: "+worldName);
        }
        if(loca!=null) {
            location = loca.getBlock().getLocation();
            shift = location.toVector().subtract(originalLoc.toVector());
        } else {
            location = originalLoc;
            shift = new Vector(0,0,0);
        }
//Logger.getGlobal().info("Shift: "+shift.getBlockX()+" "+shift.getBlockY()+" "+shift.getBlockZ());
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
        Vector originalSize = new Vector(in.readInt(),in.readInt(),in.readInt());
//Logger.getGlobal().info("OrininalSize: "+originalSize.getBlockX()+" "+originalSize.getBlockY()+" "+originalSize.getBlockZ());
        if(size==null) {
            size = originalSize;
        }
        final Vector finalSize = size;
        if(!size.equals(originalSize)) {
            throw new InvalidRestoreDataException("Unexpected Restore Data Size");
        }
        resultSize = size;
        final LocalTransformation rotation = new LocalTransformation(location.toVector(),size,rotations,flip);
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
//Logger.getGlobal().info("finalSize: "+finalSize.getBlockX()+" "+finalSize.getBlockY()+" "+finalSize.getBlockZ());
        int maxX;
        int maxZ;
        if(rotations%2==1) {
            maxX=location.getBlockX()+size.getBlockZ();
            maxZ=location.getBlockZ()+size.getBlockX();
        } else {
            maxX=location.getBlockX()+size.getBlockX();
            maxZ=location.getBlockZ()+size.getBlockZ();
        }
//Logger.getGlobal().log(Level.INFO,"area: {0} {1} {2} ---- {3} {4} {5} ",
//                                new Object[]{location.getBlockX(),location.getBlockY(),location.getBlockZ(),
//                                             maxX,location.getBlockY()+size.getBlockY(),maxZ});
        Collection<Entity> entities = location.getWorld()
                 .getNearbyEntities(new BoundingBox(location.getBlockX(),
                                                    location.getBlockY(),
                                                    location.getBlockZ(),
                                                    maxX,
                                                    location.getBlockY()+size.getBlockY(),
                                                    maxZ),
                        new MCMEEntityFilter());
        for(Entity entity: entities) {
            entity.remove();
        }

        int paletteLength = in.readInt();
//Logger.getGlobal().info("Palette: "+paletteLength);
        Map<Integer,BlockData> palette = new HashMap<>(paletteLength);
        for(int i = 0; i<paletteLength; i++) {
            int dataLength = in.readInt();
            byte[] byteData = new byte[dataLength];
            in.readFully(byteData);
            String blockDataString;
            if(legacyBlocks) {
                blockDataString = legacyBlockMappings(new String(byteData, StandardCharsets.UTF_8));
            } else {
                blockDataString = new String(byteData, StandardCharsets.UTF_8);
            }
            blockDataString = blockMappings(blockDataString);
            BlockData blockData = Bukkit.getServer()
                                        .createBlockData(blockDataString);
            palette.put(i, rotation.transformBlockData(blockData));
        }
        int biomePaletteLength = in.readInt();
        Map<Integer,Biome> biomePalette = new HashMap<>(biomePaletteLength);
        for (int i = 0; i < biomePaletteLength; i++) {
            int dataLength = in.readInt();
            byte[] byteData = new byte[dataLength];
            in.readFully(byteData);
            String biomeName = new String(byteData, StandardCharsets.UTF_8);
            //Logger.getGlobal().info("Biome name: "+biomeName);
            Biome biome = Biome.PLAINS;
            try {
                biome = Registry.BIOME.get(NamespacedKey.minecraft(biomeName.toLowerCase()));//get(Registry.BIOME.getKey())//Biome.valueOf(biomeName);
            } catch (IllegalArgumentException ignore) {}
            //Logger.getGlobal().info("Biome palette entry: "+i+" "+biome);
            biomePalette.put(i, biome);
        }
//log("location",location);
        Vector rotatedLowCorner = rotation.transformVector(location.toVector(),true);
//log("rotated", rotatedLowCorner);
//log("size",size);
        Vector rotatedHighCorner = rotation.transformVector(location.toVector()
                                              .add(size).subtract(new Vector(1,1,1)),true);
//log("rotatedHigh", rotatedHighCorner);
        int xIncrement = (rotatedLowCorner.getBlockX()<=rotatedHighCorner.getBlockX()?1:-1);
//Logger.getGlobal().info("xinc " +xIncrement);
        int zIncrement = (rotatedLowCorner.getBlockZ()<=rotatedHighCorner.getBlockZ()?1:-1);
//Logger.getGlobal().info("zinc " +zIncrement);
        int firstStart, firstEnd, firstInc, secondStart, secondEnd, secondInc;
        int temp;
        if(rotations%2==0) {
            firstStart = rotatedLowCorner.getBlockX();
            firstEnd = rotatedHighCorner.getBlockX();
            firstInc = xIncrement;
            secondStart = rotatedLowCorner.getBlockZ();
            secondEnd = rotatedHighCorner.getBlockZ();
            secondInc = zIncrement;
        } else {
            firstStart = rotatedLowCorner.getBlockZ();
            firstEnd = rotatedHighCorner.getBlockZ();
            firstInc = zIncrement;
            secondStart = rotatedLowCorner.getBlockX();
            secondEnd = rotatedHighCorner.getBlockX();
            secondInc = xIncrement;
        }
        BlockData air = Bukkit.createBlockData(Material.AIR);
        for(int i = firstStart;
                        i != firstEnd+firstInc;
                                i=i+firstInc) {
            for(int j = secondStart;
                        j != secondEnd+secondInc;
                                j=j+secondInc) {
                int x,z;
                if(rotations%2==0) {
                    x=i;
                    z=j;
                } else {
                    x=j;
                    z=i;
                }
                int biomeIndex = in.readInt();
//Logger.getGlobal().info("biome index: "+biomeIndex);
                Biome biome = biomePalette.get(biomeIndex);
//Logger.getGlobal().info(""+biome);
                if(biome != null && withBiome) {
                    location.getWorld().setBiome(x, z, biome);
                }
                int maxY = in.readInt() + shift.getBlockY();
                int yStart = location.getBlockY();
                int yEnd = yStart+size.getBlockY();
                int yInc = 1;
                int columnCountMax = Math.min(yEnd,maxY)-yStart;
                int columnCount = 0;
                if(flip[1]) {
//Logger.getGlobal().info("flip y");
                    temp = yStart;
                    yStart = yEnd-1;
                    yEnd = temp-1;
                    yInc = -1;
                }
                for(int y = yStart; y != yEnd; y = y+ yInc) {
                    //Location loc = rotation.rotateVector(new Vector(x, y, z),true).toLocation(location.getWorld());
//Logger.getGlobal().info("Block: "+location.getWorld().getName()+" "+x+" "+y+" "+z+" "+location.getBlock().getType()+" maxY "+maxY+" cC "+columnCount+" ccM "+columnCountMax);
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{location.getBlockX(),location.getBlockY(),location.getBlockZ()});
                    //if(y<=maxY) {
                    if(columnCount <= columnCountMax) {
                        BlockData data = palette.get(in.readInt());
                        if(withAir || !data.getMaterial().equals(Material.AIR)) {
                            location.getWorld().getBlockAt(x, y, z).setBlockData(data,false);
                        }
                        columnCount++;
                    } else {
                        if(withAir) {
                            location.getWorld().getBlockAt(x, y, z).setBlockData(air,false);
                        }
                    }
                }
            }
        }
        int tileEntityLength = in.readInt();
        final List tileEntityDatas = new ArrayList();
        try {
            for (int i = 0; i < tileEntityLength; i++) {
                Object nbt = AccessNBT.readNBTFromStream(in);
                tileEntityDatas.add(nbt);
            }
        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
            for (Object nbt : tileEntityDatas) {
                try {
                    Object nmsWorld = AccessCraftBukkit.getWorldServer(location.getWorld());
                    Object blockposition = AccessWorld.getTileEntityBlockPosition(nbt);
                    Object newPosition = AccessCore.shiftBlockPosition(blockposition, shift.getBlockX(), shift.getBlockY(), shift.getBlockZ());
                    final Vector rotatedVector = rotation.transformVector(AccessCore.toVector(newPosition), true);
                    newPosition = AccessCore.toBlockPosition(rotatedVector);
                    AccessNBT.setInt(nbt, "x", AccessCore.getBlockPositionX(newPosition));
                    AccessNBT.setInt(nbt, "y", AccessCore.getBlockPositionY(newPosition));
                    AccessNBT.setInt(nbt, "z", AccessCore.getBlockPositionZ(newPosition));

                    Object chunk = AccessWorld.getChunkAtWorldCoords(nmsWorld, newPosition);
                    //IBlockState a_(BlockPosition)
                    Object iBlockState = AccessWorld.getBlockState(chunk, newPosition);
//Logger.getGlobal().info("loading nbt TileEntity: " + nbt.toString());
                    //static TileEntity a(BlockPosition, IBlockData, NBTTagCompound)
                    Object entity = AccessWorld.createTileEntity(newPosition, iBlockState, nbt);
                    if (entity != null) {
                        //void a(TileEntity)
                        AccessWorld.setTileEntity(chunk, entity);
                    } else {
                        Logger.getLogger(MCMEPlotFormat.class.getSimpleName()).info("Warning! Tile entity skipped!");
                    }
                } catch (ClassNotFoundException | SecurityException | IllegalArgumentException | NullPointerException ex) {
                    Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        final int entityLength = in.readInt();
        final List entityDatas = new ArrayList();
        try {
            for (int i = 0; i < entityLength; i++) {
                entityDatas.add(AccessNBT.readNBTFromStream(in));
            }
        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        for (Object nbt : entityDatas) {
            try {

                //move to new position
                //List<NBTTagList c(String, int)
                Object list = AccessNBT.getNBTBaseList(nbt, "Pos");

                double[] position = new double[3];
                for (int i = 0; i < 3; i++) {
                    //double h(int)
                    position[i] = AccessNBT.getDoubleFromList(list, i);
                }
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                Vector newPosition = rotation.transformVector(new Vector(position[0] + shift.getBlockX(),
                                position[1] + shift.getBlockY(),
                                position[2] + shift.getBlockZ()),
                        false);
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{newPosition.getBlockX(),newPosition.getBlockY(),newPosition.getBlockZ()});
                //boolean a(int, NBTBase)
                AccessNBT.setNBTBaseInList(list,0, AccessNBT.createNBTTagDouble(newPosition.getX()));
                AccessNBT.setNBTBaseInList(list,1, AccessNBT.createNBTTagDouble(newPosition.getY()));
                AccessNBT.setNBTBaseInList(list,2, AccessNBT.createNBTTagDouble(newPosition.getZ()));
                //list.a(2,new NBTTagDouble(list.k(2)+shift.getBlockZ()));
                //NBTBase a(String, NBTbase)
                AccessNBT.setNBTBase(nbt, "Pos", list);

                //rotate entity
                //NBTTagList c(String, int);
                Object rotList = AccessNBT.getFloatList(nbt, "Rotation");
                //float i(int)
                float yaw = AccessNBT.getFloatFromList(rotList, 0);
                //boolean a(int, NBTBase)
                AccessNBT.setNBTBaseInList(rotList, 0, AccessNBT.createNBTTagFloat(rotation.transformYaw(yaw)) );
                //NBTBase a(String, NBTBase)
                AccessNBT.setNBTBase(nbt, "Rotation", rotList);

                //rotate facing of hanging entities
                //String l(String)
                String type = AccessNBT.getString(nbt, "id");
                Byte facing = 0;
//Logger.getGlobal().info("id: "+type);
                if (type.equals("minecraft:painting") || type.equals("minecraft:item_frame") || type.equals("minecraft:glow_item_frame")) {
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                    //byte f(String)
                    boolean lowercaseFacing;
                    if(AccessNBT.hasKey(nbt, "Facing")) {
                        facing = AccessNBT.getByte(nbt, "Facing");
                        lowercaseFacing = false;
                    } else {
                        facing = AccessNBT.getByte(nbt, "facing");
                        lowercaseFacing = true;
                    }
//Logger.getGlobal().info("hanging: "+facing);
                    Byte transformedFacing = rotation.transformHangingEntity(type, facing);
//Logger.getGlobal().info("hanging rot: "+transformedFacing);
                    Object nbtFacing = AccessNBT.createNBTTagByte(transformedFacing);
                    //NBTBase a(String, NBTBase)
                    if(lowercaseFacing) {
                        AccessNBT.setNBTBase(nbt, "facing", nbtFacing);
                    } else {
                        AccessNBT.setNBTBase(nbt, "Facing", nbtFacing);
                    }
                    if (type.equals("minecraft:item_frame") || type.equals("minecraft:glow_item_frame") /*&& transformedFacing < 2*/) {
//Logger.getGlobal().info("item rotation");
                        //byte f(String)
                        Byte itemRot = AccessNBT.getByte(nbt, "ItemRotation");
//Logger.getGlobal().info("itemFrame: "+newPosition.getX()+" "+newPosition.getY()+" "+newPosition.getZ()+" "+facing +" "+itemRot);
                        itemRot = rotation.transformItemRotation(facing, itemRot);
//Logger.getGlobal().info("transformed item rot: "+itemRot);
                        Object nbtItemRot = AccessNBT.createNBTTagByte(itemRot);
                        //NBTBaste a(String, NBTBase)
                        AccessNBT.setNBTBase(nbt, "ItemRotation", nbtItemRot);
                    }
                }

                //put Tile Tags for hanging entities.
                int tileX = tileCoord(newPosition.getX());
                int tileY = tileCoord(newPosition.getY());
                int tileZ = tileCoord(newPosition.getZ());
//Logger.getGlobal().info("NewX: "+newPosition.getX() + " NewZ: "+newPosition.getZ());
//Logger.getGlobal().info("TileX: "+tileX + " TileZ: "+tileZ);
                Object nbtTileX = AccessNBT.createNBTTagInt(tileX);
                Object nbtTileY = AccessNBT.createNBTTagInt(tileY);
                Object nbtTileZ = AccessNBT.createNBTTagInt(tileZ);
                //NBTBaste a(String, NBTBase)
                AccessNBT.setNBTBase(nbt, "TileX", nbtTileX);
                AccessNBT.setNBTBase(nbt, "TileY", nbtTileY);
                AccessNBT.setNBTBase(nbt, "TileZ", nbtTileZ);

                //give random UUID to entity
                UUID uuid = UUID.randomUUID();
                int[] uuidIntArrayRepresentation = new int[4];
                uuidIntArrayRepresentation[0] = (int) (uuid.getMostSignificantBits() >> 32);
                uuidIntArrayRepresentation[1] = (int) uuid.getMostSignificantBits();
                uuidIntArrayRepresentation[2] = (int) (uuid.getLeastSignificantBits() >> 32);
                uuidIntArrayRepresentation[3] = (int) uuid.getLeastSignificantBits();
                Object nbtIntArray = AccessNBT.createNBTTagIntArray(uuidIntArrayRepresentation);
                Object nbtLeast = AccessNBT.createNBTTagLong(uuid.getLeastSignificantBits());
                Object nbtMost = AccessNBT.createNBTTagLong(uuid.getMostSignificantBits());
                //NBTBaste a(String, NBTBase)
                AccessNBT.setNBTBase(nbt, "UUIDLeast", nbtLeast);
                AccessNBT.setNBTBase(nbt, "UUIDLeast", nbtMost); //"UUIDMost"??? Probably a derp!
                AccessNBT.setNBTBase(nbt, "UUID", nbtIntArray);

                //WorldServer getHandle()
                Object nmsWorld = AccessCraftBukkit.getWorldServer(location.getWorld());
Logger.getGlobal().info("NBT: "+nbt);
                Object entity = AccessWorld.createEntity(nmsWorld, nbt);
Logger.getGlobal().info("ENTITY: "+entity);

                //add entity to world
                AccessServer.addFreshEntity(nmsWorld, entity);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Location high = location.clone().add(finalSize.toLocation(location.getWorld()));
        //NMSUtil.updatePlayerChunks(location, high);
    }
    
    private static int tileCoord(double entityCoord) {
        if(entityCoord>=0) {
            return (int) (entityCoord-0.0000001d);
        } else {
            return (int) (entityCoord-0.0000001d)-1;
        }
    }
      
    private void log(String name, Location loc) {
        Logger.getGlobal().info(name+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
    }
    private void log(String name, Vector loc) {
        Logger.getGlobal().info(name+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
    }

    private String legacyBlockMappings(String blockData) {
        if(blockData.contains("level")) {
            if(blockData.contains("level=0")) {
                blockData = blockData.replace("[level=0]","");
            } else {
                blockData = blockData.replace("cauldron", "water_cauldron");
            }
        }
        return blockData;
    }

    private String blockMappings(String blockData) {
        if(blockData.equals("minecraft:grass")) {
            return "minecraft:short_grass";
        }
        return blockData;
    }

}
