package dev.blackdev.mixin;
import dev.blackdev.log.PacketLogger;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void packetspy$onSend(Packet<?> packet, CallbackInfo ci) { PacketLogger.get().logOutbound(packet); }
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("TAIL"))
    private void packetspy$onRead(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) { PacketLogger.get().logInbound(packet); }
}
