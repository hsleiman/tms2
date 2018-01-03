/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated;

import static com.objectbrains.tms.constants.Constants.IVR_PATH;
import com.objectbrains.tms.service.FreeswitchConfiguration;

/**
 *
 * @author hsleiman
 */
public enum RecordedNames {

    AARON,
    ABIGAIL,
    ABRAHAM,
    ADAM,
    ADAMS,
    ADOLPH,
    ADRIAN,
    ADRIANA,
    ADRIANNA,
    AFTON,
    AGUILAR,
    AKERS,
    ALAN,
    ALBERT,
    ALBERTA,
    ALCORN,
    ALEXANDER,
    ALEXANDRA,
    ALFREDO,
    ALICE,
    ALISON,
    ALLAN,
    ALLEEN,
    ALLEN,
    ALLISON,
    ALLYSON,
    ALMA,
    ALSOP,
    ALVA,
    AMANDA,
    AMELIA,
    AMY,
    ANDERSON,
    ANDREA,
    ANDREW,
    ANGEL,
    ANGELA,
    ANGELES,
    ANGELICA,
    ANIBAL,
    ANITRA,
    ANN,
    ANNA,
    ANNE,
    ANNIE,
    ANTHONY,
    ANTONE,
    ANTWAN,
    ARCHIE,
    ARDEN,
    ARIAS,
    ARLENA,
    ARLIE,
    ARMSTRONG,
    ARNOLD,
    ARTHUR,
    ASHLEY,
    AUBREY,
    AUDREY,
    AURELIO,
    AUSTIN,
    AVA,
    AVERY,
    BABARA,
    BABIN,
    BAGWELL,
    BAILEY,
    BAKER,
    BALL,
    BALLARD,
    BARB,
    BARBARA,
    BARBOSA,
    BARNES,
    BART,
    BARTON,
    BATES,
    BAUER,
    BEALL,
    BEATRICE,
    BEAU,
    BECKER,
    BELL,
    BELLA,
    BENITO,
    BENJAMIN,
    BENNETT,
    BENNIE,
    BERNADETTE,
    BERNICE,
    BERRY,
    BERT,
    BERTRAM,
    BESS,
    BESSIE,
    BETANCOURT,
    BETHANY,
    BETSEY,
    BETTY,
    BEVERLY,
    BILLY,
    BIRDIE,
    BISHOP,
    BITTNER,
    BLACK,
    BLAKE,
    BLANCA,
    BOB,
    BOBBY,
    BOLES,
    BOND,
    BONNIE,
    BORIS,
    BOTTOMS,
    BOWEN,
    BOWER,
    BOYD,
    BRADLEY,
    BRADY,
    BRANDON,
    BRANDY,
    BRENDA,
    BRENDAN,
    BRENNER,
    BRENT,
    BREWER,
    BRIAN,
    BRITT,
    BROCKMAN,
    BROOK,
    BROOKS,
    BROWN,
    BROYLES,
    BRUCE,
    BRUNO,
    BRYANT,
    BRYON,
    BUCHANAN,
    BUCKLAND,
    BUD,
    BUFORD,
    BUNTING,
    BURGESS,
    BURGOS,
    BURKE,
    BURNETTE,
    BURROWS,
    BURT,
    BUTLER,
    BUTTERFIELD,
    BYNUM,
    BYRD,
    CALVIN,
    CAMERON,
    CAMILLA,
    CAMILLE,
    CAMPBELL,
    CARL,
    CARLOS,
    CARLTON,
    CARNEY,
    CARO,
    CAROL,
    CAROLINE,
    CAROLYN,
    CARR,
    CARRIER,
    CARSON,
    CARTER,
    CARTWRIGHT,
    CASEY,
    CASON,
    CASSIE,
    CATHERINE,
    CATHRYN,
    CHANELLE,
    CHANTAL,
    CHAPMAN,
    CHARLES,
    CHARLINE,
    CHARLOTTE,
    CHARLTON,
    CHENG,
    CHERYL,
    CHLOE,
    CHONG,
    CHRIS,
    CHRISTENSEN,
    CHRISTIAN,
    CHRISTIE,
    CHRISTINA,
    CHRISTINE,
    CHRISTOPHER,
    CHUNG,
    CHURCHILL,
    CLAIRE,
    CLANTON,
    CLARENCE,
    CLARK,
    CLARKE,
    CLARKSON,
    CLEMENCIA,
    CLOSE,
    COBB,
    COBLE,
    CODY,
    COLEMAN,
    COLIN,
    COLLIER,
    COLLINS,
    CONLEY,
    CONNER,
    CONNOR,
    CONSTANCE,
    COOK,
    COOPER,
    COREY,
    CORNELIUS,
    CORNISH,
    CORREIA,
    COUNTS,
    COX,
    CRAIG,
    CRAYTON,
    CREEL,
    CREIGHTON,
    CRISTIE,
    CRISTINA,
    CROSBY,
    CRUZ,
    CUMMINGS,
    CURRY,
    CYNTHIA,
    DAISY,
    DALE,
    DALLAS,
    DAN,
    DANIEL,
    DANIELS,
    DARA,
    DAREN,
    DARLENE,
    DARRYL,
    DAVID,
    DAVIDSON,
    DAVIES,
    DAVIS,
    DEANE,
    DEBORAH,
    DEBRA,
    DEIRDRE,
    DELACRUZ,
    DELGADO,
    DELMER,
    DEMARCUS,
    DENISE,
    DENNA,
    DENNIS,
    DEREK,
    DEVIN,
    DEWEY,
    DIANA,
    DIANE,
    DIAZ,
    DICKENS,
    DIXIE,
    DOMENIC,
    DOMINGO,
    DOMINIC,
    DONALD,
    DONN,
    DONNA,
    DONNIE,
    DOREEN,
    DORI,
    DORIS,
    DOROTHY,
    DOUGLAS,
    DOUGLASS,
    DOWD,
    DOWLING,
    DOWNEY,
    DOWNING,
    DOWNS,
    DOYLE,
    DRAKE,
    DREW,
    DRUMMOND,
    DUNCAN,
    DUNLAP,
    DUSTIN,
    DWAIN,
    DYER,
    DYLAN,
    EARL,
    EDDIE,
    EDMUNDS,
    EDWARD,
    EDWARDS,
    ELDRIDGE,
    ELENA,
    ELISEO,
    ELIZABETH,
    ELLA,
    ELLIOTT,
    ELLISON,
    ELLSWORTH,
    ELMO,
    ELOY,
    ELSIE,
    ELTON,
    ELVIN,
    ELVINA,
    ELVIRA,
    EMANUEL,
    EMERSON,
    EMILIO,
    EMILY,
    EMMA,
    EMORY,
    ENNIS,
    ENRIQUEZ,
    ERIC,
    ERICA,
    ERMA,
    ERNEST,
    ERVIN,
    ERWIN,
    ESSIE,
    ESTEBAN,
    ESTELLE,
    ESTHER,
    ESTRADA,
    ETHELYN,
    EUGENE,
    EUSEBIO,
    EVAN,
    EVANS,
    EVELYN,
    FABIAN,
    FAITH,
    FARMER,
    FELICIA,
    FELICITA,
    FELICITY,
    FELIX,
    FERGUSON,
    FERMIN,
    FILOMENA,
    FIONA,
    FISHER,
    FITE,
    FLEMING,
    FLETCHER,
    FLORENTINO,
    FLORES,
    FLOYD,
    FOLSE,
    FORSYTH,
    FOSTER,
    FOX,
    FRANCES,
    FRANCIS,
    FRANK,
    FRANKLIN,
    FRASER,
    FRED,
    FREDDY,
    FREDERICA,
    FREDERICK,
    FREDIA,
    FREDRIC,
    FREDRICK,
    FREEMAN,
    FRENCH,
    FRIDA,
    FULLER,
    GABRIELLE,
    GALLAGHER,
    GAMBLE,
    GARCIA,
    GARNER,
    GARY,
    GATEWOOD,
    GAVIN,
    GENEVA,
    GEOFFREY,
    GEORGE,
    GEORGIANNA,
    GERALD,
    GERARD,
    GIBSON,
    GILBERT,
    GILL,
    GILLEY,
    GINGER,
    GIRARD,
    GLORIA,
    GLOVER,
    GONZALES,
    GONZALEZ,
    GOODE,
    GORDON,
    GRACE,
    GRAF,
    GRAHAM,
    GRANT,
    GRAY,
    GRAZYNA,
    GREEN,
    GREENE,
    GREENLEE,
    GREENWOOD,
    GREER,
    GREGORY,
    GRICE,
    GRIER,
    GRIFFIN,
    GUERRERO,
    GWEN,
    HA,
    HAHN,
    HALL,
    HAMILTON,
    HAMM,
    HANES,
    HANKS,
    HANNAH,
    HARDACRE,
    HARDY,
    HARMON,
    HAROLD,
    HARPER,
    HARRELL,
    HARRIS,
    HARRY,
    HART,
    HAYDEN,
    HEATHER,
    HELEN,
    HELLER,
    HEMMINGS,
    HENDERSON,
    HENRY,
    HERBERT,
    HERNANDEZ,
    HERRING,
    HICKS,
    HIEDI,
    HILL,
    HILLS,
    HINES,
    HISAKO,
    HOANG,
    HODGES,
    HOFFMAN,
    HOGG,
    HOLCOMBE,
    HOLDER,
    HOLIDAY,
    HOLLIDAY,
    HOLLOWAY,
    HOLLY,
    HOLMES,
    HORSLEY,
    HOWARD,
    HOWELL,
    HUBBARD,
    HUDGENS,
    HUDSON,
    HUFF,
    HUGHES,
    HUNTER,
    HUSKEY,
    HYMAN,
    IAN,
    INCE,
    INEZ,
    IRENE,
    IRWIN,
    ISAAC,
    ISIDRO,
    JACK,
    JACKIE,
    JACKSON,
    JACOB,
    JACOBS,
    JACOBSEN,
    JACQUELINE,
    JACQUES,
    JAKE,
    JAMAAL,
    JAMAR,
    JAMES,
    JAMISON,
    JAN,
    JANE,
    JANET,
    JANICE,
    JASMINE,
    JASON,
    JEAN,
    JEANNIE,
    JEFF,
    JEFFREY,
    JENKINS,
    JENNIFER,
    JENNINGS,
    JEREMIAH,
    JEREMY,
    JERMAINE,
    JEROLD,
    JEROMY,
    JERROD,
    JERRY,
    JESSE,
    JESSICA,
    JESTINE,
    JETTA,
    JILL,
    JIMENEZ,
    JIMMY,
    JO,
    JOAN,
    JOANNE,
    JODY,
    JOE,
    JOETTE,
    JOHN,
    JOHNATHAN,
    JOHNNY,
    JOHNSON,
    JOHNSTON,
    JONATHAN,
    JONATHON,
    JONES,
    JORGENSEN,
    JOSE,
    JOSEF,
    JOSEPH,
    JOSEPHINE,
    JOSHUA,
    JOSIAH,
    JOSIE,
    JOYCE,
    JUAN,
    JUANA,
    JUDITH,
    JUDSON,
    JUDY,
    JULIA,
    JULIAN,
    JULIANNA,
    JULIE,
    JULIET,
    JULIUS,
    JUNE,
    JUNG,
    JUSTIN,
    KAREN,
    KARL,
    KASEY,
    KATELIN,
    KATHERINE,
    KATHLEEN,
    KATHRYN,
    KATHY,
    KAY,
    KEITH,
    KELLER,
    KELLEY,
    KELLY,
    KENDAL,
    KENNETH,
    KENNY,
    KENT,
    KERR,
    KEVIN,
    KIM,
    KIMBERLY,
    KIMIKO,
    KING,
    KIP,
    KIRKPATRICK,
    KITCHENS,
    KLEIN,
    KNOTT,
    KNOX,
    KRAIG,
    KRISTINA,
    KRISTOPHER,
    KYLE,
    KYLIE,
    LACY,
    LADNER,
    LAKE,
    LAMBERT,
    LANGDON,
    LANGE,
    LARA,
    LARRY,
    LARUE,
    LASHAUNDA,
    LATOYA,
    LAURA,
    LAUREN,
    LAURENCE,
    LAURIE,
    LAVERNE,
    LAWRENCE,
    LEA,
    LEAH,
    LEE,
    LEHMAN,
    LEIA,
    LEIGH,
    LEIGHA,
    LEMON,
    LEO,
    LEONARD,
    LEONIDA,
    LEROY,
    LESLIE,
    LEVI,
    LEWIS,
    LIAM,
    LILES,
    LILLIAN,
    LILY,
    LINDA,
    LINDSEY,
    LISA,
    LISSA,
    LOGAN,
    LOIS,
    LONG,
    LOPEZ,
    LORA,
    LORENA,
    LORENZO,
    LORI,
    LOUIS,
    LOUISE,
    LOVETT,
    LOY,
    LU,
    LUCAS,
    LUCIO,
    LUCY,
    LUIS,
    LUKE,
    LULA,
    LYMAN,
    LYNCH,
    LYNDA,
    LYNWOOD,
    MABEL,
    MACDONALD,
    MACK,
    MACKAY,
    MACKENZIE,
    MACKLIN,
    MACLEOD,
    MADALINE,
    MADDEN,
    MADELEINE,
    MAE,
    MAEGAN,
    MAES,
    MAIA,
    MALDONADO,
    MANN,
    MANNING,
    MARCELINE,
    MARCELO,
    MARCH,
    MARCIA,
    MARCO,
    MARGARET,
    MARGIE,
    MARIA,
    MARIANELA,
    MARIE,
    MARILYN,
    MARK,
    MARKLEY,
    MARSH,
    MARSHA,
    MARSHALL,
    MARTHA,
    MARTI,
    MARTIN,
    MARTINEZ,
    MARY,
    MARYLYNN,
    MASON,
    MASSEY,
    MATHIS,
    MATT,
    MATTHEW,
    MAURICIO,
    MAX,
    MAY,
    MAYS,
    MCCARTHY,
    MCCLINTOCK,
    MCCLURE,
    MCCOMBS,
    MCCORKLE,
    MCDONALD,
    MCGEE,
    MCGRATH,
    MCKINLEY,
    MCLAIN,
    MCLEAN,
    MEAGHAN,
    MEDEIROS,
    MEGAN,
    MEGGAN,
    MEGHANN,
    MELANIE,
    MELISSA,
    MELODIE,
    MENDOZA,
    MERLIN,
    METCALFE,
    MICHAEL,
    MICHEL,
    MICHELLE,
    MILDRED,
    MILLER,
    MILLS,
    MINERVA,
    MIQUEL,
    MISTY,
    MITCHELL,
    MIZE,
    MOHAMMAD,
    MOLLY,
    MONTANO,
    MONTES,
    MONTGOMERY,
    MONTY,
    MOONEY,
    MOORE,
    MORGAN,
    MORIN,
    MORRIS,
    MORRISON,
    MOSHE,
    MUHAMMAD,
    MURPHY,
    MURRAY,
    MUSGROVE,
    MYRLE,
    MYRTLE,
    NANCY,
    NAOMI,
    NASH,
    NATALIE,
    NATHAN,
    NEAL,
    NEEL,
    NEIL,
    NELSON,
    NETTIE,
    NEVILLE,
    NEWMAN,
    NEWTON,
    NGUYEN,
    NICHOLAS,
    NICHOLE,
    NICK,
    NICKOLAS,
    NICOLA,
    NICOLE,
    NIELSEN,
    NOLAN,
    NOLAND,
    NORA,
    NORMA,
    NORRIS,
    NORTH,
    NORTON,
    NOWLIN,
    NUNEZ,
    OBRIEN,
    OCTAVIO,
    OFELIA,
    OGDEN,
    OLIVER,
    OLIVIA,
    OLSON,
    OMER,
    OPAL,
    OSBORNE,
    OSWALD,
    OTTO,
    OWEN,
    PADILLA,
    PAGE,
    PAIGE,
    PAM,
    PAMELA,
    PARKER,
    PARR,
    PARSONS,
    PAT,
    PATERSON,
    PATRICIA,
    PATRICK,
    PATTERSON,
    PATTI,
    PATTON,
    PAUL,
    PAULA,
    PAULINE,
    PAYNE,
    PEAK,
    PEAKE,
    PEARSON,
    PEDRO,
    PEGGY,
    PENDLETON,
    PENELOPE,
    PENNINGTON,
    PEPPER,
    PERALTA,
    PEREZ,
    PERRY,
    PETER,
    PETERS,
    PETERSON,
    PHIL,
    PHILIP,
    PHILLIP,
    PHILLIPS,
    PHYLLIS,
    PIERS,
    PIPER,
    PIPPA,
    PLANTE,
    POOLE,
    PORSCHE,
    POWELL,
    PRATT,
    PRESLEY,
    PRESTON,
    PRICE,
    PULLMAN,
    QUINN,
    RACHEL,
    RAFAEL,
    RAINEY,
    RALPH,
    RAMIREZ,
    RAMPLING,
    RAMSEY,
    RANDALL,
    RANDOLPH,
    RANDY,
    RAQUEL,
    RAYFORD,
    RAYMOND,
    REAVES,
    REBECCA,
    REED,
    REES,
    REEVES,
    REID,
    REINALDO,
    REINHART,
    RENETTA,
    REYES,
    REYNALDO,
    REYNOLDS,
    RHEBA,
    RICHARD,
    RICHARDS,
    RICHARDSON,
    RICKETTS,
    RICKEY,
    RITCHIE,
    RIVERA,
    ROB,
    ROBERT,
    ROBERTS,
    ROBERTSON,
    ROBINSON,
    ROBT,
    RODGER,
    RODRIGO,
    RODRIGUEZ,
    ROGER,
    ROGERS,
    ROLF,
    ROMAN,
    ROMERO,
    RONALD,
    RONNA,
    ROSE,
    ROSEN,
    ROSENDO,
    ROSS,
    ROTH,
    ROWE,
    ROXANNE,
    ROY,
    ROYAL,
    ROYCE,
    RUBEN,
    RUBY,
    RUSSELL,
    RUTH,
    RUTHERFORD,
    RYAN,
    SABRINA,
    SALLY,
    SAM,
    SAMANTHA,
    SAMUAL,
    SAMUEL,
    SANCHEZ,
    SANDERS,
    SANDERSON,
    SANDRA,
    SANTIAGO,
    SANTOS,
    SARA,
    SARAH,
    SCHMIDT,
    SCOTT,
    SEAMAN,
    SEAN,
    SEBASTIAN,
    SETH,
    SHAD,
    SHANE,
    SHANI,
    SHARILYN,
    SHARON,
    SHARP,
    SHARRON,
    SHAW,
    SHAWN,
    SHELDON,
    SHEPHERD,
    SHERMAN,
    SHIRA,
    SHIRLEY,
    SHORT,
    SIERRA,
    SIKES,
    SILVIA,
    SIMMONS,
    SIMON,
    SIMONE,
    SIMONS,
    SIMPSON,
    SIMS,
    SINGLETON,
    SKINNER,
    SLATER,
    SLEDGE,
    SMITH,
    SOL,
    SONIA,
    SOPHIE,
    SORIA,
    SOWERS,
    SPRINGER,
    SPRUILL,
    STACEY,
    STACY,
    STANLEY,
    STEELE,
    STEGALL,
    STEPHANIE,
    STEPHEN,
    STEVE,
    STEVEN,
    STEVENS,
    STEVENSON,
    STEWART,
    STOKES,
    STOUT,
    STRICKLAND,
    STROTHER,
    SUE,
    SULEMA,
    SULLIVAN,
    SUNG,
    SUSAN,
    SUSANA,
    SUSANNAH,
    SUTHERLAND,
    SWANK,
    TALBOT,
    TAMISHA,
    TAMMERA,
    TAMMY,
    TAYLOR,
    TENNEY,
    TERESA,
    TERRI,
    TERRY,
    TESS,
    THERESA,
    THOMAS,
    THOMPSON,
    THOMSEN,
    THOMSON,
    TILLERY,
    TIM,
    TIMMY,
    TIMOTHY,
    TINA,
    TINISHA,
    TODD,
    TOLER,
    TONIE,
    TORO,
    TORRES,
    TORRI,
    TOVA,
    TRACEY,
    TRAN,
    TRAVERS,
    TREJO,
    TRENTON,
    TREVOR,
    TRIBBLE,
    TROUT,
    TRUDEAU,
    TUCKER,
    TUGGLE,
    TURNER,
    TUTTLE,
    UNA,
    UNDERWOOD,
    URBINA,
    VALENTI,
    VALERIE,
    VALLES,
    VANCE,
    VANESA,
    VANESSA,
    VAUGHAN,
    VERDA,
    VETA,
    VICKERS,
    VICTOR,
    VICTORIA,
    VILLASENOR,
    VILLEGAS,
    VINCENT,
    VINNIE,
    VIOLET,
    VIRGINIA,
    VIVIENNE,
    WADE,
    WAGNER,
    WAHL,
    WALDO,
    WALKER,
    WALLACE,
    WALLING,
    WALSH,
    WALTER,
    WALTON,
    WANDA,
    WARD,
    WARFIELD,
    WARREN,
    WASHINGTON,
    WATERS,
    WATSON,
    WAYNE,
    WEATHERFORD,
    WEBB,
    WEBER,
    WELCH,
    WELLS,
    WENDY,
    WES,
    WEST,
    WESTER,
    WESTON,
    WHITE,
    WHITNEY,
    WILBUR,
    WILKINS,
    WILLIAM,
    WILLIAMS,
    WILLIAMSON,
    WILLIE,
    WILLIS,
    WILLOUGHBY,
    WILSON,
    WOOD,
    WORLEY,
    WRAY,
    WRIGHT,
    WYATT,
    YARBROUGH,
    YEE,
    YOLANDA,
    YOUNG,
    YVETTE,
    YVONNE,
    ZENOBIA,
    ZIMMERMAN,
    ZOE,
    ZUNIGA;

    public String getAudioPath() {
        return (IVR_PATH + FreeswitchConfiguration.getIVRVoice() + "/" + FreeswitchConfiguration.getIVRRate() + "/names/" + name() + ".wav");
    }
    
    public static RecordedNames getName(String name){
        for(RecordedNames rn: RecordedNames.values()){
            if(name.equalsIgnoreCase(rn.toString())){
                return rn;
            }
        }
        return null;
    }
}
