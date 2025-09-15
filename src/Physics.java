public class Physics {
    public float findForce (float mass , float acceleration) {
        return mass * acceleration;
    }
    public float findAcceleration (float force , float mass) {
        return force / mass;
    }
    public float findMass (float force , float acceleration) {
        return force / acceleration;
    }
    public Vector3f findNetForce (Vector3f[] forces) {
        Vector3f netForce = new Vector3f(0, 0, 0);
        for (Vector3f force: forces) {
            netForce.sub( force);
        }
        return new Vector3f(0, 0, 0);
    }
    
}
