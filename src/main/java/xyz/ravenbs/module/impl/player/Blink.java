package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    
    public Blink() {
        super("Blink", ModuleCategory.player);
    }
    
    @Override
    public void onEnable() {
        packets.clear();
    }

    @Override
    public void onDisable() {
        if (mc.getNetworkHandler() != null) {
            while (!packets.isEmpty()) {
                // Need a way to send without triggering event loop if using hook.
                // Assuming FakeLag issue, we might need direct access.
                // For now, re-sending might re-trigger.
                // We should add a 'ignore' flag to the event or threadlocal.
                // Or just:
                Packet<?> p = packets.poll();
                mc.getNetworkHandler().getConnection().send(p, null); 
            }
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            e.setCancelled(true);
            packets.add(e.getPacket());
        }
    }
}
