package xyz.ravenbs.module;

/** Higher priorities run first for motion, packet, update, and render callbacks. */
public enum ModuleEventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}
