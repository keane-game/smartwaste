export class Geometry {


    constructor(coordinates: any,geometryType= "string",
        spatialReference =  "string",
        geometryRing = "string",
        ) {
        this.geometryType = geometryType;
        this.spatialReference = spatialReference;
        this.geometryRing =geometryRing;
        this.coordinates = coordinates;
    }

    geometryType: string;
    spatialReference?: string;
    geometryRing: string;
    coordinates: any;
}
