// Suppress "Critical dependency: the request of a dependency is an expression"
// warnings produced by Ktor/Skiko dynamic imports in the Kotlin/Wasm output.
config.module = config.module || {};
config.module.exprContextCritical = false;
