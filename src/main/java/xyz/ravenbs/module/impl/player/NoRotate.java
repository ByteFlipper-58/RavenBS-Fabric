package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.ReceivePacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate", ModuleCategory.player);
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            // This packet forces rotation. We can change the yaw/pitch in the packet to current yaw/pitch
            // But Packet fields are final or private. Accessor needed?
            // Actually, PlayerPositionLookS2CPacket has public getters but no setters.
            // We'd need to cancel and construct a new one or use Accessor.
            // For now, let's just leave it as a stub or cancel completely (dangerous, causes desync).
        }
    }
}
