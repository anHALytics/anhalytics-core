#!/bin/sh

# OPTIONS:
#    -help                        print command line options
#    -harvestDaily                harvest the publications of the yesterday
#    -harvest                     harvest the publications, if no date limitation process all repository content
#    -grobid                      extract TEIs from the downladed binaries buildTei
#    -buildTei                    build the corpus tei containing the mtds and the fulltext tei
#    -annotate                    process named entities using the NERD service
#    -index {tei|annotation}      index either the full tei or annotation into ES
#    -dFromDate "yyyy-mm-dd"      set/filter records to process by starting date
#    -dUntilDate "yyyy-mm-dd"     set/filter records to process by ending date


if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=`which java`
fi

if [ ! -x "$JAVA" ]; then
    echo "Could not find any executable java binary. Please install java in your PATH or set JAVA_HOME"
    exit 1
fi


# Print command line usage / help
usage() {
	echo "Usage: $0 [-action] [args]"
	echo "   -help                        print command line options"
	echo "   -harvestDaily                harvest the publications of the yesterday"
	echo "   -harvest                     harvest the publications, if no date limitation process all repository content"
	echo "   -grobid                      extract TEIs from the downladed binaries buildTei"
	echo "   -mineData                    extract data from teis and insert them into DB"
	echo "   -generateTei                 build the corpus tei containing the mtds and the fulltext tei"
	echo "   -annotate                    process named entities using the NERD service"
	echo "   -index {tei|annotation}      index either the full tei or annotation into ES"
	echo "   -dFromDate yyyy-mm-dd        set/filter records to process by starting date"
	echo "   -dUntilDate yyyy-mm-dd       set/filter records to process by ending date"
}

array_contains2 () {
    local in=1
    local array="$1[@]"
    local seeking=$2

    for element in "${!array}"; do
        if [[ $element == $seeking ]]; then
            in=0
            break
        fi
    done
    return $in
}

check_dates_option(){
    dates=("-dFromDate" "-dUntilDate")
    if array_contains2 arr "$1" || [ -z "$1" ] 
        then return 0 
    else return 1
    fi
}

for p in ./anhalytics-*/target
do
    if [ "$(ls -A $p)" ]; then
        continue;
    else
        echo "Make sur the project is compiled."
        exit 0
    fi
done



for m in ./anhalytics-*/target/*.one-jar.jar
do
    if [[ $m == *"harvest"* ]]
        then
            modules[0]=$m
    elif [[ $m == *"annotate"* ]]
        then
            modules[1]=$m
    elif [[ $m == *"index"* ]]
        then
            modules[2]=$m
    fi
done

while true; 
do
    case $1 in
	
        -help)
            usage
            exit 0
            ;;
        -harvestDaily)
            "$JAVA" -Xmx2048m -jar "${modules[0]}" -exe harvestDaily
            exit 0
            ;;
        -harvest)
            if check_dates_option "$2" || check_dates_option "$4"
                then
            "$JAVA" -Xmx2048m -jar "${modules[0]}" -exe harvestAll $2 $3 $4 $5
            else usage
            fi
            exit 0
            ;;
        -grobid)
            if check_dates_option "$2" || check_dates_option "$4"
                then
            "$JAVA" -Xmx2048m -jar "${modules[0]}" -exe processGrobid $2 $3 $4 $5
            else usage
            fi
            exit 0
            ;;
        -generateTei)
            if check_dates_option "$2" || check_dates_option "$4"
                then
            "$JAVA" -Xmx2048m -jar "${modules[0]}" -exe generateTei $2 $3 $4 $5
            else usage
            fi
            exit 0
            ;;
        -annotate)
            if check_dates_option "$2" || check_dates_option "$4"
                then
            "$JAVA" -Xmx2048m -jar "${modules[1]}" -multiThread $2 $3 $4 $5
            else usage
            fi
            exit 0
            ;;
        -index)
            arr=( "tei" "annotation" )
            if array_contains2 arr "$2"
                then
                    "$JAVA" -Xmx2048m -jar "${modules[2]}" -index $2
            else usage
            fi
            exit 0
            ;;
        *)
            echo "Error parsing argument $1!" >&2
            usage
            exit 1
        ;;
    esac
done