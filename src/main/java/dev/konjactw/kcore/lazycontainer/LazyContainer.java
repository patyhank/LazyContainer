package dev.konjactw.kcore.lazycontainer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicLong;

public final class LazyContainer extends JavaPlugin {

    private static final boolean SHADOW =
            Boolean.getBoolean("lazycontainer.shadow");

    public static final AtomicLong stash = new AtomicLong();
    public static final AtomicLong ensure = new AtomicLong();
    public static final AtomicLong rawSave = new AtomicLong();
    public static final AtomicLong eagerLoad = new AtomicLong();
    public static final AtomicLong shadowMismatch = new AtomicLong();
    public static final AtomicLong benignReorder = new AtomicLong();

    public static boolean shadow() {
        return SHADOW;
    }

    public static void onStash() {
        stash.incrementAndGet();
    }

    public static void onEnsure() {
        ensure.incrementAndGet();
    }

    public static void onRawSave() {
        rawSave.incrementAndGet();
    }

    public static void onEagerLoad() {
        eagerLoad.incrementAndGet();
    }

    public static void onShadowMismatch() {
        shadowMismatch.incrementAndGet();
    }

    public static void onBenignReorder(
            String pos,
            String raw,
            String eager
    ) {
        benignReorder.incrementAndGet();
    }

    public static void dumpMismatch(
            String pos,
            String raw,
            String eager
    ) {
    }
}