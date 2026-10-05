package dev.cd.shpricefix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Substitutes rift drop ids SkyHanni cannot price with the crafted item it should track.
 * Master toggle defaults off. Each substitution defaults on, and only applies while the master is on.
 */
public final class PriceFix {
    public static final String TARGET_CLASS = "at.hannibal2.skyhanni.utils.ItemPriceUtils";
    public static final String PRICE_METHOD = "getPriceOrNull-0mM9I0c";
    public static final String SKYHANNI_MOD_ID = "skyhanni";

    public static final String THE_ONE_BUNDLE = "ENCHANTED_BOOK_BUNDLE_THE_ONE";
    public static final String THE_ONE_4 = "ULTIMATE_THE_ONE;4";
    public static final String UNFANGED_PART = "UNFANGED_VAMPIRE_PART";
    public static final String DENTIST_RELIC = "VAMPIRE_DENTIST_RELIC";
    public static final String QUANTUM_BUNDLE = "ENCHANTED_BOOK_BUNDLE_QUANTUM";
    public static final String QUANTUM_3 = "QUANTUM;3";

    public static final String REQUIRES_SKYHANNI = "Profit Tracker Fix requires SkyHanni.";
    public static final String INCOMPATIBLE_SKYHANNI = "Incompatible SkyHanni version - fix inactive.";

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("shpricefix.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile boolean enabled;
    private static volatile boolean theOneBundle = true;
    private static volatile boolean unfanged = true;
    private static volatile boolean quantum = true;
    private static volatile boolean patchActive;

    private PriceFix() {
    }

    /**
     * @return a chat warning when a saved enabled state had to be turned off, otherwise null
     */
    public static String load() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            return null;
        }
        try {
            Config config = GSON.fromJson(Files.readString(CONFIG_PATH), Config.class);
            if (config == null) {
                return null;
            }
            theOneBundle = config.theOneBundle;
            unfanged = config.unfanged;
            quantum = config.quantum;
            if (!config.enabled) {
                enabled = false;
                return null;
            }
            String failure = incompatibility();
            if (failure != null) {
                enabled = false;
                save();
                return failure;
            }
            enabled = true;
            return null;
        } catch (Exception e) {
            ShPriceFix.LOGGER.warn("Could not read shpricefix config, using defaults", e);
            return null;
        }
    }

    /**
     * @return a chat error when enabling is refused, otherwise null
     */
    public static String setEnabled(boolean on) {
        if (!on) {
            enabled = false;
            save();
            return null;
        }
        String failure = incompatibility();
        if (failure != null) {
            enabled = false;
            save();
            return failure;
        }
        enabled = true;
        save();
        return null;
    }

    public static void setTheOneBundle(boolean on) {
        theOneBundle = on;
        save();
    }

    public static void setUnfanged(boolean on) {
        unfanged = on;
        save();
    }

    public static void setQuantum(boolean on) {
        quantum = on;
        save();
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean theOneBundle() {
        return theOneBundle;
    }

    public static boolean unfanged() {
        return unfanged;
    }

    public static boolean quantum() {
        return quantum;
    }

    public static String targetFor(String sourceId) {
        if (!enabled || sourceId == null) {
            return null;
        }
        return switch (sourceId) {
            case THE_ONE_BUNDLE -> theOneBundle ? THE_ONE_4 : null;
            case UNFANGED_PART -> unfanged ? DENTIST_RELIC : null;
            case QUANTUM_BUNDLE -> quantum ? QUANTUM_3 : null;
            default -> null;
        };
    }

    public static void markPatched() {
        patchActive = true;
    }

    public static boolean isPatched() {
        return patchActive;
    }

    public static boolean probePriceMethod() {
        try {
            for (Method method : Class.forName(TARGET_CLASS).getDeclaredMethods()) {
                if (PRICE_METHOD.equals(method.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isSkyhanniLoaded() {
        try {
            return FabricLoader.getInstance().isModLoaded(SKYHANNI_MOD_ID);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String incompatibility() {
        if (!isSkyhanniLoaded()) {
            return REQUIRES_SKYHANNI;
        }
        if (!probePriceMethod()) {
            return INCOMPATIBLE_SKYHANNI;
        }
        return null;
    }

    private static void save() {
        Config config = new Config();
        config.enabled = enabled;
        config.theOneBundle = theOneBundle;
        config.unfanged = unfanged;
        config.quantum = quantum;
        Path tmp = CONFIG_PATH.resolveSibling("shpricefix.json.tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(tmp, GSON.toJson(config));
            try {
                Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            ShPriceFix.LOGGER.warn("Could not save shpricefix config", e);
        }
    }

    private static final class Config {
        public boolean enabled = false;
        public boolean theOneBundle = true;
        public boolean unfanged = true;
        public boolean quantum = true;
    }
}
