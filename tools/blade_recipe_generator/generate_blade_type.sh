#!/usr/bin/env bash
# =============================================================================
# generate_blade_type.sh
# Generates all recipe JSON files for a new blade/tool type under
#   src/main/resources/data/epicoverknights/recipes/**
#
# Usage:
#   ./tools/blade_recipe_generator/generate_blade_type.sh --type <name> [options]
#
# See --help for all options.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

TEMPLATES_DIR="${SCRIPT_DIR}/templates"
CONFIG_DIR="${SCRIPT_DIR}/config"
MATERIALS_FILE="${CONFIG_DIR}/materials.tsv"
DATA_ROOT="${ROOT_DIR}/src/main/resources/data/epicoverknights/recipes"

# ---------------------------------------------------------------------------
# Defaults
# ---------------------------------------------------------------------------
TYPE=""
DRY_RUN=0
FORCE=0
WITH_KNAPPING=1
WITH_STONE_FORGING=0
WITH_SMITHING=1
WITH_TOOLTYPES=1
SMITHING_BASE_MATERIAL="steel"
SMITHING_RESULT_MATERIAL="diamond"
SMITHING_ADDITION_ITEM="minecraft:diamond"
SMITHING_TEMPLATE_ITEM="overgeared:diamond_upgrade_smithing_template"
TOOLTYPE_INCLUDE_STONE=0
CRAFTING_HANDLE="magistuarmory:hilt"
HAMMERING=3
CASTING_AMOUNT=9

# ---------------------------------------------------------------------------
usage() {
  cat <<'EOF'
Usage:
  ./tools/blade_recipe_generator/generate_blade_type.sh --type <name> [options]

Required:
  --type <name>            New blade/tool type name (e.g. shortsword, saber)

Options:
  --dry-run                Print what would be written without creating files
  --force                  Overwrite already existing files
  --with-knapping          Generate stone knapping recipe      (default: ON)
  --without-knapping       Skip stone knapping recipe
  --with-stone-forging     Generate stone forging recipe       (default: OFF)
  --without-stone-forging  Skip stone forging recipe
  --with-smithing          Generate smithing upgrade recipe    (default: ON)
  --without-smithing       Skip smithing upgrade recipe
  --with-tooltypes         Generate item_to_tooltype recipe    (default: ON)
  --without-tooltypes      Skip item_to_tooltype recipe
  --smithing-base <mat>    Base weapon material for smithing   (default: steel)
  --smithing-result <mat>  Result weapon material              (default: diamond)
  --smithing-addition <id> Smithing addition item id           (default: minecraft:diamond)
  --smithing-template <id> Smithing template item id           (default: overgeared:diamond_upgrade_smithing_template)
  --crafting-handle <id>   Handle item id in crafting recipe   (default: magistuarmory:hilt)
  --hammering <n>          Hammer strikes in forging minigame  (default: 3)
  --casting-amount <n>     Nugget units required for casting   (default: 9, i.e. 1 ingot; use ingots*9)
  --include-stone-tooltype Include stone blade in tooltypes    (default: OFF)
  --help                   Show this help

Customisation:
  Edit templates in  tools/blade_recipe_generator/templates/*.tpl
  Edit materials in  tools/blade_recipe_generator/config/materials.tsv

Placeholders used in templates:
  __TYPE__                  blade type (e.g. shortsword)
  __MATERIAL__              material   (e.g. iron)
  __TIER__                  forging tier
  __EXPERIENCE__            casting experience value
  __FORGING_KEY_KIND__      "item" or "tag"
  __FORGING_KEY_VALUE__     the actual item/tag id
  __BLADE_ITEM__            generated blade item id
  __WEAPON_ITEM__           generated finished weapon item id
  __SMITHING_BASE_ITEM__    smithing base weapon item id
  __SMITHING_RESULT_ITEM__  smithing result weapon item id
  __SMITHING_ADDITION_ITEM__ smithing addition item id
  __SMITHING_TEMPLATE_ITEM__ smithing template item id
  __TOOLTYPE_ITEMS__        formatted item list for tooltypes template
EOF
}

# ---------------------------------------------------------------------------
# Parse arguments
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --type)               TYPE="${2:-}";    shift 2 ;;
    --dry-run)            DRY_RUN=1;        shift   ;;
    --force)              FORCE=1;          shift   ;;
    --with-knapping)      WITH_KNAPPING=1;  shift   ;;
    --without-knapping)   WITH_KNAPPING=0;  shift   ;;
    --with-stone-forging) WITH_STONE_FORGING=1; shift ;;
    --without-stone-forging) WITH_STONE_FORGING=0; shift ;;
    --with-smithing)      WITH_SMITHING=1; shift ;;
    --without-smithing)   WITH_SMITHING=0; shift ;;
    --with-tooltypes)     WITH_TOOLTYPES=1; shift ;;
    --without-tooltypes)  WITH_TOOLTYPES=0; shift ;;
    --smithing-base)      SMITHING_BASE_MATERIAL="${2:-}"; shift 2 ;;
    --smithing-result)    SMITHING_RESULT_MATERIAL="${2:-}"; shift 2 ;;
    --smithing-addition)  SMITHING_ADDITION_ITEM="${2:-}"; shift 2 ;;
    --smithing-template)  SMITHING_TEMPLATE_ITEM="${2:-}"; shift 2 ;;
    --crafting-handle)    CRAFTING_HANDLE="${2:-}"; shift 2 ;;
    --hammering)          HAMMERING="${2:-}"; shift 2 ;;
    --casting-amount)     CASTING_AMOUNT="${2:-}"; shift 2 ;;
    --include-stone-tooltype) TOOLTYPE_INCLUDE_STONE=1; shift ;;
    --help|-h)            usage; exit 0    ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

# ---------------------------------------------------------------------------
# Validate input
# ---------------------------------------------------------------------------
if [[ -z "${TYPE}" ]]; then
  echo "Error: --type is required." >&2
  usage
  exit 1
fi

if [[ ! -f "${MATERIALS_FILE}" ]]; then
  echo "Error: materials file not found: ${MATERIALS_FILE}" >&2
  exit 1
fi

for tpl in forging.json.tpl casting_blasting.json.tpl casting_furnace.json.tpl \
           casting_smelting.json.tpl knapping_stone.json.tpl crafting.json.tpl \
           smithing.json.tpl tooltypes.json.tpl; do
  if [[ ! -f "${TEMPLATES_DIR}/${tpl}" ]]; then
    echo "Error: missing template: ${TEMPLATES_DIR}/${tpl}" >&2
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
maybe_mkdir() {
  local d="$1"
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    echo "[dry-run] mkdir -p ${d}"
  else
    mkdir -p "${d}"
  fi
}

# render_template <template> <output_path>
# Uses shell variables: TYPE MATERIAL TIER EXPERIENCE FORGING_KEY_KIND FORGING_KEY_VALUE
render_template() {
  local tpl="$1"
  local out="$2"

  if [[ "${DRY_RUN}" -eq 1 ]]; then
    echo "[dry-run] write ${out}"
    return 0
  fi

  if [[ -f "${out}" && "${FORCE}" -ne 1 ]]; then
    echo "  skip (exists) ${out}"
    return 0
  fi

  # Read template and replace all placeholders
  local content
  content="$(cat "${tpl}")"
  content="${content//__TYPE__/${TYPE}}"
  content="${content//__MATERIAL__/${MATERIAL}}"
  content="${content//__TIER__/${TIER}}"
  content="${content//__EXPERIENCE__/${EXPERIENCE}}"
  content="${content//__FORGING_KEY_KIND__/${FORGING_KEY_KIND}}"
  content="${content//__FORGING_KEY_VALUE__/${FORGING_KEY_VALUE}}"
  content="${content//__BLADE_ITEM__/${BLADE_ITEM}}"
  content="${content//__WEAPON_ITEM__/${WEAPON_ITEM}}"
  content="${content//__SMITHING_BASE_ITEM__/${SMITHING_BASE_ITEM}}"
  content="${content//__SMITHING_RESULT_ITEM__/${SMITHING_RESULT_ITEM}}"
  content="${content//__SMITHING_ADDITION_ITEM__/${SMITHING_ADDITION_ITEM}}"
  content="${content//__SMITHING_TEMPLATE_ITEM__/${SMITHING_TEMPLATE_ITEM}}"
  content="${content//__TOOLTYPE_ITEMS__/${TOOLTYPE_ITEMS}}"
  content="${content//__CRAFTING_HANDLE__/${CRAFTING_HANDLE}}"
  content="${content//__HAMMERING__/${HAMMERING}}"
  content="${content//__CASTING_AMOUNT__/${CASTING_AMOUNT}}"

  printf '%s\n' "${content}" > "${out}"
  echo "  wrote ${out#${ROOT_DIR}/}"
}

# ---------------------------------------------------------------------------
# Prepare output directories
# ---------------------------------------------------------------------------
FORGING_DIR="${DATA_ROOT}/forging/${TYPE}_blade"
CASTING_DIR="${DATA_ROOT}/casting/${TYPE}_blade"
CRAFTING_DIR="${DATA_ROOT}/crafting/${TYPE}"
KNAPPING_DIR="${DATA_ROOT}/knapping"
SMITHING_DIR="${DATA_ROOT}/smithing"
TOOLTYPES_DIR="${DATA_ROOT}/tooltypes"

maybe_mkdir "${FORGING_DIR}"
maybe_mkdir "${CASTING_DIR}"
maybe_mkdir "${CRAFTING_DIR}"
[[ "${WITH_KNAPPING}" -eq 1 ]] && maybe_mkdir "${KNAPPING_DIR}"
[[ "${WITH_SMITHING}" -eq 1 ]] && maybe_mkdir "${SMITHING_DIR}"
[[ "${WITH_TOOLTYPES}" -eq 1 ]] && maybe_mkdir "${TOOLTYPES_DIR}"

TOOLTYPE_ITEMS=""
SMITHING_BASE_ITEM=""
SMITHING_RESULT_ITEM=""
BLADE_ITEM=""
WEAPON_ITEM=""
# ---------------------------------------------------------------------------
# Process materials from TSV
# columns: material  tier  experience  key_kind  key_value  forge_enabled  casting_enabled  crafting_enabled
# ---------------------------------------------------------------------------
echo "Generating recipes for blade type: ${TYPE}"
echo "----------------------------------------------"

while IFS=$'\t' read -r MATERIAL TIER EXPERIENCE FORGING_KEY_KIND FORGING_KEY_VALUE \
                         FORGE_ENABLED CASTING_ENABLED CRAFTING_ENABLED; do

  # Skip blank lines and comment lines
  [[ -z "${MATERIAL}"        ]] && continue
  [[ "${MATERIAL:0:1}" == "#" ]] && continue

  echo "[${MATERIAL}]"

  BLADE_ITEM="epicoverknights:${MATERIAL}_${TYPE}_blade"
  WEAPON_ITEM="magistuarmory:${MATERIAL}_${TYPE}"

  if [[ "${MATERIAL}" != "stone" || "${TOOLTYPE_INCLUDE_STONE}" -eq 1 ]]; then
    if [[ -n "${TOOLTYPE_ITEMS}" ]]; then
      TOOLTYPE_ITEMS+=","
      TOOLTYPE_ITEMS+=$'\n'
    fi
    TOOLTYPE_ITEMS+="    {"
    TOOLTYPE_ITEMS+=$'\n'
    TOOLTYPE_ITEMS+="      \"item\": \"${BLADE_ITEM}\""
    TOOLTYPE_ITEMS+=$'\n'
    TOOLTYPE_ITEMS+="    }"
  fi

  # -- Forging ---------------------------------------------------------------
  if [[ "${FORGE_ENABLED}" == "1" ]]; then
    if [[ "${MATERIAL}" == "stone" && "${WITH_STONE_FORGING}" -ne 1 ]]; then
      echo "  skip forging (stone forging disabled; use --with-stone-forging to enable)"
    else
      render_template \
        "${TEMPLATES_DIR}/forging.json.tpl" \
        "${FORGING_DIR}/${MATERIAL}_${TYPE}_blade.json"
    fi
  fi

  # -- Casting ---------------------------------------------------------------
  if [[ "${CASTING_ENABLED}" == "1" ]]; then
    render_template \
      "${TEMPLATES_DIR}/casting_blasting.json.tpl" \
      "${CASTING_DIR}/${MATERIAL}_${TYPE}_blade_from_cast_blasting.json"
    render_template \
      "${TEMPLATES_DIR}/casting_furnace.json.tpl" \
      "${CASTING_DIR}/${MATERIAL}_${TYPE}_blade_from_cast_furnace.json"
    render_template \
      "${TEMPLATES_DIR}/casting_smelting.json.tpl" \
      "${CASTING_DIR}/${MATERIAL}_${TYPE}_blade_from_cast_smelting.json"
  fi

  # -- Crafting --------------------------------------------------------------
  if [[ "${CRAFTING_ENABLED}" == "1" ]]; then
    render_template \
      "${TEMPLATES_DIR}/crafting.json.tpl" \
      "${CRAFTING_DIR}/${MATERIAL}_${TYPE}.json"
  fi

done < "${MATERIALS_FILE}"

# ---------------------------------------------------------------------------
# Stone knapping (separate, always uses stone material vars)
# ---------------------------------------------------------------------------
if [[ "${WITH_KNAPPING}" -eq 1 ]]; then
  echo "[stone knapping]"
  MATERIAL="stone"
  TIER="stone"
  EXPERIENCE="0.0"
  FORGING_KEY_KIND="item"
  FORGING_KEY_VALUE="minecraft:cobblestone"
  render_template \
    "${TEMPLATES_DIR}/knapping_stone.json.tpl" \
    "${KNAPPING_DIR}/stone_${TYPE}_blade.json"
fi

if [[ "${WITH_SMITHING}" -eq 1 ]]; then
  echo "[smithing]"
  MATERIAL="${SMITHING_BASE_MATERIAL}"
  BLADE_ITEM="epicoverknights:${MATERIAL}_${TYPE}_blade"
  WEAPON_ITEM="magistuarmory:${MATERIAL}_${TYPE}"
  SMITHING_BASE_ITEM="magistuarmory:${SMITHING_BASE_MATERIAL}_${TYPE}"
  SMITHING_RESULT_ITEM="magistuarmory:${SMITHING_RESULT_MATERIAL}_${TYPE}"
  render_template \
    "${TEMPLATES_DIR}/smithing.json.tpl" \
    "${SMITHING_DIR}/${SMITHING_BASE_MATERIAL}_${TYPE}_to_${SMITHING_RESULT_MATERIAL}_${TYPE}.json"
fi

if [[ "${WITH_TOOLTYPES}" -eq 1 ]]; then
  echo "[tooltypes]"
  render_template \
    "${TEMPLATES_DIR}/tooltypes.json.tpl" \
    "${TOOLTYPES_DIR}/${TYPE}_to_tooltype.json"
fi

echo "----------------------------------------------"
echo "Done."

