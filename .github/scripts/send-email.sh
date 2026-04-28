#!/usr/bin/env bash
set -euo pipefail

usage() {
    echo "Usage: $0 <api_key> <from_email> <template_file> <to_email> [<to_email>...] [KEY=val KEY=val ...]" >&2
    echo "Example: $0 \"re_key\" \"from@test.com\" \".github/scripts/templates/failure.txt\" \"to@test.com\" REPO=\"myrepo\" BRANCH=\"dev\"" >&2
    exit 1
}

render_template() {
    local template_file="$1"
    shift

    if [[ ! -f "$template_file" ]]; then
        echo "Error: Template file not found: $template_file" >&2
        exit 1
    fi

    local content
    content=$(cat "$template_file")

    # Replace {VARIABLE} placeholders with env var values
    while [[ "$content" =~ \{[A-Z_]+\} ]]; do
        local var_name="${BASH_REMATCH[0]:1:-1}"
        local var_value="${!var_name:-}"
        content="${content//"$BASH_REMATCH[0]"/"$var_value"}"
    done

    echo "$content"
}

validate_params() {
    if [[ -z "${1:-}" ]] || [[ -z "${2:-}" ]] || [[ -z "${3:-}" ]]; then
        echo "Error: Missing required parameters" >&2
        usage
    fi
}

parse_json_field() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed 's/.*:[[:space:]]*"\([^"]*\)"/\1/'
}

build_to_array() {
    local to_emails=("$@")
    local to_json="["
    local first=true
    for email in "${to_emails[@]}"; do
        if [[ -n "$email" ]]; then
            if [[ "$first" == "true" ]]; then
                first=false
            else
                to_json+=","
            fi
            to_json+="\"$email\""
        fi
    done
    to_json+="]"
    echo "$to_json"
}

validate_params "${1:-}" "${2:-}" "${3:-}"

API_KEY="$1"
FROM_EMAIL="$2"
TEMPLATE_FILE="$3"
shift 3

# Collect to_emails until we hit KEY=val pattern
TO_EMAILS=()
while [[ $# -gt 0 ]]; do
    if [[ "$1" =~ ^[A-Za-z_][A-Za-z0-9_]*=.+$ ]]; then
        break
    fi
    TO_EMAILS+=("$1")
    shift
done

if [[ ${#TO_EMAILS[@]} -eq 0 ]]; then
    echo "Error: At least one to_email is required" >&2
    usage
fi

RENDERED=$(render_template "$TEMPLATE_FILE")

SUBJECT=$(parse_json_field "$RENDERED" "subject")
HTML_CONTENT=$(parse_json_field "$RENDERED" "html")

if [[ -z "$SUBJECT" ]]; then
    echo "Error: Could not parse 'subject' from template" >&2
    exit 1
fi

if [[ -z "$HTML_CONTENT" ]]; then
    echo "Error: Could not parse 'html' from template" >&2
    exit 1
fi

TO_JSON=$(build_to_array "${TO_EMAILS[@]}")

RESPONSE=$(
    curl -s -X POST "https://api.resend.com/emails" \
        -H "Authorization: Bearer $API_KEY" \
        -H "Content-Type: application/json" \
        -d "{
            \"from\": \"$FROM_EMAIL\",
            \"to\": $TO_JSON,
            \"subject\": \"$SUBJECT\",
            \"html\": \"$HTML_CONTENT\"
        }"
)

if echo "$RESPONSE" | grep -q '"id"'; then
    echo "Email sent successfully"
else
    echo "Error: Failed to send email. Response: $RESPONSE" >&2
    exit 1
fi
