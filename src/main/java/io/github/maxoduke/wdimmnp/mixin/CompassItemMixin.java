package io.github.maxoduke.wdimmnp.mixin;

import io.github.maxoduke.wdimmnp.items.LinkedCompassItem;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompassItem.class)
public class CompassItemMixin
{
    @Inject(at = @At("RETURN"), method = "useOnBlock", cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir)
    {
        if (LinkedCompassItem._useOnBlock(context))
            cir.setReturnValue(ActionResult.success(true));
    }
}
