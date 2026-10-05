package dev.cd.shpricefix.mixin;

import at.hannibal2.skyhanni.utils.ItemPriceSource;
import at.hannibal2.skyhanni.utils.ItemPriceUtils;
import dev.cd.shpricefix.PriceFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.List;

@Mixin(targets = "at.hannibal2.skyhanni.utils.ItemPriceUtils", remap = false)
public abstract class ItemPriceUtilsMixin {

    @Inject(
        target = @Desc(
            value = PriceFix.PRICE_METHOD,
            args = {String.class, ItemPriceSource.class, List.class},
            ret = Double.class
        ),
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private void shpricefix$substituteRiftPrice(
        String internalName,
        ItemPriceSource priceSource,
        List<?> pastRecipes,
        CallbackInfoReturnable<Double> cir
    ) {
        String target;
        try {
            target = PriceFix.targetFor(internalName);
        } catch (Throwable ignored) {
            return;
        }
        if (target == null || target.equals(internalName)) {
            return;
        }

        try {
            Method priceMethod = ItemPriceUtils.INSTANCE.getClass()
                .getMethod(PriceFix.PRICE_METHOD, String.class, ItemPriceSource.class, List.class);
            Object result = priceMethod.invoke(ItemPriceUtils.INSTANCE, target, priceSource, pastRecipes);
            if (result instanceof Double price) {
                cir.setReturnValue(price);
                try {
                    PriceFix.markPatched();
                } catch (Throwable ignored) {
                }
            }
            // null substitute (e.g. price API down): fall through to original.
        } catch (Throwable ignored) {
            // Never break SkyHanni pricing from here.
        }
    }
}
