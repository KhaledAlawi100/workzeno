/**
 * Backward-compatible export.
 *
 * The actual team hook belongs to the collaboration feature.
 * This export allows existing project pages to keep working
 * without immediately changing their imports.
 */
export { useProjectTeam } from "../../collaboration/membership/hooks/useProjectTeam";
