package eu.decentsoftware.holograms.nms.v1_16_R3;

import com.mojang.datafixers.util.Pair;
import eu.decentsoftware.holograms.shared.DecentPosition;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R3.PacketPlayOutMount;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.craftbukkit.v1_16_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class EntityPacketsBuilder {

    private final List<Packet<?>> packets;

    private EntityPacketsBuilder() {
        this.packets = new ArrayList<>();
    }

    void sendTo(Player player) {
        for (Packet<?> packet : packets) {
            sendPacket(player, packet);
        }
    }

    EntityPacketsBuilder withSpawnEntityLivingOrObject(int entityId, EntityType type, DecentPosition position) {
        if (type.isAlive()) {
            return withSpawnEntityLiving(entityId, type, position);
        } else {
            return withSpawnEntity(entityId, type, position);
        }
    }

    EntityPacketsBuilder withSpawnEntity(int entityId, EntityType type, DecentPosition position) {
        PacketDataSerializerWrapper serializer = prepareSpawnEntityData(entityId, type, position);
        serializer.writeInt(type == EntityType.DROPPED_ITEM ? 1 : 0);
        serializer.writeShort(0);
        serializer.writeShort(0);
        serializer.writeShort(0);

        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity();
        serializer.writeToPacket(packet);

        packets.add(packet);
        return this;
    }

    EntityPacketsBuilder withSpawnEntityLiving(int entityId, EntityType type, DecentPosition position) {
        PacketDataSerializerWrapper serializer = prepareSpawnEntityData(entityId, type, position);
        serializer.writeByte(MathHelper.d(position.getYaw() * 256.0F / 360.0F));
        serializer.writeShort(0);
        serializer.writeShort(0);
        serializer.writeShort(0);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
        serializer.writeToPacket(packet);

        packets.add(packet);
        return this;
    }

    private PacketDataSerializerWrapper prepareSpawnEntityData(int entityId, EntityType type, DecentPosition position) {
        PacketDataSerializerWrapper serializer = PacketDataSerializerWrapper.getInstance();
        serializer.writeVarInt(entityId);
        serializer.writeUuid(UUID.randomUUID());
        serializer.writeByte(EntityTypeRegistry.getEntityTypeId(type));
        serializer.writeDouble(position.getX());
        serializer.writeDouble(position.getY());
        serializer.writeDouble(position.getZ());
        serializer.writeByte(MathHelper.d(position.getYaw() * 256.0F / 360.0F));
        serializer.writeByte(MathHelper.d(position.getPitch() * 256.0F / 360.0F));
        return serializer;
    }

    EntityPacketsBuilder withEntityMetadata(int entityId, List<DataWatcher.Item<?>> watchableObjects) {
        PacketDataSerializerWrapper serializer = PacketDataSerializerWrapper.getInstance();
        serializer.writeVarInt(entityId);
        serializer.writeWatchableObjects(watchableObjects);

        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
        serializer.writeToPacket(packet);

        packets.add(packet);
        return this;
    }

    EntityPacketsBuilder withHelmet(int entityId, ItemStack itemStack) {
        Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> equipmentPair = new Pair<>(
                CraftEquipmentSlot.getNMS(EquipmentSlot.HEAD),
                itemStackToNms(itemStack)
        );
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                entityId,
                Collections.singletonList(equipmentPair)
        );
        packets.add(packet);
        return this;
    }

    EntityPacketsBuilder withTeleportEntity(int entityId, DecentPosition position) {
        PacketDataSerializerWrapper serializer = PacketDataSerializerWrapper.getInstance();
        serializer.writeVarInt(entityId);
        serializer.writeDouble(position.getX());
        serializer.writeDouble(position.getY());
        serializer.writeDouble(position.getZ());
        serializer.writeByte((byte) ((int) (position.getYaw() * 256.0F / 360.0F)));
        serializer.writeByte((byte) ((int) (position.getPitch() * 256.0F / 360.0F)));
        serializer.writeBoolean(false); // onGround

        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
        serializer.writeToPacket(packet);
        packets.add(packet);
        return this;
    }

    EntityPacketsBuilder withPassenger(int entityId, int passenger) {
        return updatePassenger(entityId, passenger);
    }

    EntityPacketsBuilder withRemovePassenger(int entityId) {
        return updatePassenger(entityId, -1);
    }

    private EntityPacketsBuilder updatePassenger(int entityId, int... passengers) {
        PacketDataSerializerWrapper serializer = PacketDataSerializerWrapper.getInstance();
        serializer.writeVarInt(entityId);
        serializer.writeIntArray(passengers);

        PacketPlayOutMount packet = new PacketPlayOutMount();
        serializer.writeToPacket(packet);

        packets.add(packet);
        return this;
    }

    EntityPacketsBuilder withRemoveEntity(int entityId) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityId);
        packets.add(packet);
        return this;
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private net.minecraft.server.v1_16_R3.ItemStack itemStackToNms(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    static EntityPacketsBuilder create() {
        return new EntityPacketsBuilder();
    }

}
