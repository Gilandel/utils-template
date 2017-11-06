/*
 * #%L
 * utils-scripts
 * %%
 * Copyright (C) 2016 - 2017 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.landel.utils.scripts;

import java.util.Date;

/**
 * Search bean.
 *
 * @since Dec 1, 2015
 * @author Gilles
 *
 */
public class PatientSearch {

    private Status status;
    private Long recordNumber;
    private String name;
    private String firstName;
    private Date birthDay;
    private Integer gender;
    private Distance distance;
    private Health health;
    private Attendance attendance;
    private Long unitId;
    private Long sectorId;
    private Long doctorId;

    /**
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the recordNumber
     */
    public Long getRecordNumber() {
        return this.recordNumber;
    }

    /**
     * @param recordNumber
     *            the recordNumber to set
     */
    public void setRecordNumber(Long recordNumber) {
        this.recordNumber = recordNumber;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the birthDay
     */
    public Date getBirthDay() {
        return this.birthDay;
    }

    /**
     * @param birthDay
     *            the birthDay to set
     */
    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    /**
     * @return the gender
     */
    public Integer getGender() {
        return this.gender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /**
     * @return the distance
     */
    public Distance getDistance() {
        return this.distance;
    }

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    /**
     * @return the health status
     */
    public Health getHealth() {
        return this.health;
    }

    /**
     * @param health
     *            the health status to set
     */
    public void setHealth(Health health) {
        this.health = health;
    }

    /**
     * @return the attendance
     */
    public Attendance getAttendance() {
        return this.attendance;
    }

    /**
     * @param attendance
     *            the attendance to set
     */
    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    /**
     * @return the unitId
     */
    public Long getUnitId() {
        return this.unitId;
    }

    /**
     * @param unitId
     *            the unitId to set
     */
    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    /**
     * @return the sectorId
     */
    public Long getSectorId() {
        return this.sectorId;
    }

    /**
     * @param sectorId
     *            the sectorId to set
     */
    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    /**
     * @return the doctorId
     */
    public Long getDoctorId() {
        return this.doctorId;
    }

    /**
     * @param doctorId
     *            the doctorId to set
     */
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("status=").append(this.status.name());
        sb.append(", recordNumber=").append(this.recordNumber);
        sb.append(", name=").append(this.name);
        sb.append(", firstName=").append(this.firstName);
        sb.append(", birthDay=").append(this.birthDay);
        sb.append(", gender=").append(this.gender);
        sb.append(", distance=").append(this.distance);
        sb.append(", health=").append(this.health);
        sb.append(", attendance=").append(this.attendance);
        sb.append(", unitId=").append(this.unitId);
        sb.append(", sectorId=").append(this.sectorId);
        sb.append(", doctorId=").append(this.doctorId);
        sb.append("]");

        return sb.toString();
    }

    /**
     * Status enum
     *
     * @since Dec 1, 2015
     * @author Gilles
     *
     */
    public enum Status {

        /**
         * Archive type
         */
        ARCHIVE,

        /**
         * Vacation
         */
        VACATION,

        /**
         * All type combined
         */
        ALL
    }

    /**
     * Health status
     *
     * @since Dec 1, 2015
     * @author Gilles
     *
     */
    public enum Health {
        /**
         * Bad
         */
        BAD,

        /**
         * Normal
         */
        NORMAL,

        /**
         * Good
         */
        GOOD
    }

    /**
     * Distance status
     *
     * @since Dec 1, 2015
     * @author Gilles
     *
     */
    public enum Distance {

        /**
         * Unknown
         */
        UNKNOWN,

        /**
         * Until 5 kilometers
         */
        KM_5,

        /**
         * Until 10 kilometers
         */
        KM_10,

        /**
         * Until 25 kilometers
         */
        KM_25,

        /**
         * Until 50 kilometers
         */
        KM_50,

        /**
         * Until 100 kilometers
         */
        KM_100,

        /**
         * Until 500 kilometers
         */
        KM_500,

        /**
         * Until 1000 kilometers
         */
        KM_1000,

        /**
         * Over 1000 kilometers
         */
        KM_OVER
    }

    /**
     * Attendance search
     *
     * @since Dec 1, 2015
     * @author Gilles
     *
     */
    public enum Attendance {

        /**
         * Tous.
         */
        ALL,

        /**
         * Indetermine.
         */
        UNKNOWN,

        /**
         * Present.
         */
        PRESENT,

        /**
         * Absent.
         */
        ABSENT
    }
}
