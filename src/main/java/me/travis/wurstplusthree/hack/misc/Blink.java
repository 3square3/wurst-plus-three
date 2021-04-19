package me.travis.wurstplusthree.hack.misc;

import me.travis.wurstplusthree.event.events.PacketEvent;
import me.travis.wurstplusthree.hack.Hack;
import me.travis.wurstplusthree.setting.type.BooleanSetting;
import me.travis.wurstplusthree.setting.type.DoubleSetting;
import me.travis.wurstplusthree.setting.type.EnumSetting;
import me.travis.wurstplusthree.setting.type.IntSetting;
import me.travis.wurstplusthree.util.MathsUtil;
import me.travis.wurstplusthree.util.elements.Timer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Hack {

    public static Blink INSTANCE;

    public Blink() {
        super("Blink", "stop packets", Category.MISC, false, false);
        INSTANCE = this;
    }

    public EnumSetting mode = new EnumSetting("mode", "Manual", Arrays.asList("Manual", "Time", "Distance", "Packets"), this);
    public BooleanSetting cPacketPlayer = new BooleanSetting("CPacketPlayer", true, this);
    public IntSetting timeLimit = new IntSetting("Time Limit", 20, 1, 500, this);
    public IntSetting packetLimit = new IntSetting("Packet Limit", 20, 1, 500, this);
    public DoubleSetting distance = new DoubleSetting("Distance", 10.0, 1.0, 100.0, this);

    private final Timer timer = new Timer();
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue();
    private EntityOtherPlayerMP entity;
    private int packetsCanceled = 0;
    private BlockPos startPos = null;

    @Override
    public void onEnable() {
        if (!nullCheck()) {
            this.entity = new EntityOtherPlayerMP(Blink.mc.world, Blink.mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(Blink.mc.player);
            this.entity.rotationYaw = Blink.mc.player.rotationYaw;
            this.entity.rotationYawHead = Blink.mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(Blink.mc.player.inventory);
            Blink.mc.world.addEntityToWorld(6942069, this.entity);
            this.startPos = Blink.mc.player.getPosition();
        } else {
            this.disable();
        }
        this.packetsCanceled = 0;
        this.timer.reset();
    }

    @Override
    public void onUpdate() {
        if (nullCheck() || this.mode.is("Time") && this.timer.passedS(this.timeLimit.getValue()) ||
                this.mode.is("Distance") && this.startPos != null && Blink.mc.player.getDistanceSq(this.startPos)
                        >= MathsUtil.square(this.distance.getValue().floatValue())
                || this.mode.is("Packets") && this.packetsCanceled >= this.packetLimit.getValue()) {
            this.disable();
        }
    }

    @Override
    public void onLogout() {
        this.disable();
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getStage() == 0 && Blink.mc.world != null && !mc.isSingleplayer()) {
            Object packet = event.getPacket();
            if (this.cPacketPlayer.getValue() && packet instanceof CPacketPlayer) {
                event.setCanceled(true);
                this.packets.add((Packet<?>) packet);
                ++this.packetsCanceled;
            }
            if (!this.cPacketPlayer.getValue()) {
                if (packet instanceof CPacketChatMessage || packet instanceof CPacketConfirmTeleport || packet instanceof CPacketKeepAlive || packet instanceof CPacketTabComplete || packet instanceof CPacketClientStatus) {
                    return;
                }
                this.packets.add((Packet<?>) packet);
                event.setCanceled(true);
                ++this.packetsCanceled;
            }
        }
    }

    @Override
    public void onDisable() {
        if (!nullCheck()) {
            Blink.mc.world.removeEntity(this.entity);
            while (!this.packets.isEmpty()) {
                Blink.mc.player.connection.sendPacket(this.packets.poll());
            }
        }
        this.startPos = null;
    }

}
