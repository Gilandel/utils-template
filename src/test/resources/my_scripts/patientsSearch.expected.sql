declare @date as DATE = ?
 declare @sectorId as NUMERIC(19,0) = ? 
declare @pPatients TABLE (id BIGINT, vacation BIT);
declare @sectorAssignment TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @historicSectorAssignment TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @sectorAssignmentVacation TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @historicSectorAssignmentVacation TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
INSERT INTO @pPatients
select id, vacation
    from MainPatient pp
    where
        ISNULL(pp.archive, 0)  !=  1
        AND ISNULL(pp.merged, 0) = 0
        ;
INSERT INTO @sectorAssignment
select a.id, pp.id, a.DTYPE, a.dateStart, a.dateEnd, pp.vacation, a.unit_id
        from assignment a
        inner join @pPatients pp on pp.id=a.MainPatient_id
            left join pp_sharedsectors ppss on pp.id = ppss.pp_id
            where
                (a.sector_id = @sectorId OR ppss.sector_id = @sectorId)
        ;
INSERT INTO @historicSectorAssignment
select a.id, pp.id, a.TYPE, a.dateStart, a.dateEnd, pp.vacation, a.unit_id
        from HistoricAssignment a
        inner join @pPatients pp on pp.id=a.MainPatient_id
            left join pp_sharedsectors ppss on pp.id = ppss.pp_id
            where
                (a.sectorId = @sectorId OR ppss.sector_id = @sectorId)
        ;
INSERT INTO @sectorAssignmentVacation
select * from @sectorAssignment r where r.vacation = 1;
INSERT INTO @historicSectorAssignmentVacation
select * from @historicSectorAssignment r where r.vacation = 1;
with
currentTempOrVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @sectorAssignment r
        where 
            r.type = 'BOUNDED_ASSIGNMENT'
            AND @date BETWEEN r.dateStart AND r.dateEnd
),
historicTemp (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @historicSectorAssignment r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND TYPE = 'TEMPORARY'
            AND @date BETWEEN r.dateStart AND r.dateEnd
),
historicVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @historicSectorAssignmentVacation r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND TYPE = 'VACATION'
            AND @date BETWEEN r.dateStart AND r.dateEnd
),
permanent (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @sectorAssignment r
        where
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicVacation s)
            AND r.TYPE = 'PERMANENT_ASSIGNMENT'
),
nextVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @sectorAssignmentVacation r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM permanent s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicVacation s)
            AND r.TYPE = 'BOUNDED_ASSIGNMENT'
            AND r.dateStart = (
                SELECT MIN(r2.dateStart)
                    from @sectorAssignmentVacation r2
                    where 
                        r.ppId = r2.ppId 
                        AND r2.TYPE = 'BOUNDED_ASSIGNMENT'
                        AND r2.dateStart > @date
            )
),
historicNextVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @historicSectorAssignmentVacation r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM permanent s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM nextVacation s)
            AND r.TYPE = 'VACATION'
            AND r.dateStart = (
                SELECT MIN(r2.dateStart)
                    from @historicSectorAssignmentVacation r2
                    where 
                        r.ppId = r2.ppId 
                        AND r2.TYPE = 'VACATION'
                        AND r2.dateStart > @date
            )
),
previousVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @sectorAssignmentVacation r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM permanent s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM nextVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicNextVacation s)
            AND r.TYPE = 'BOUNDED_ASSIGNMENT'
            AND r.dateEnd = (
                SELECT MAX(r2.dateEnd)
                    from @sectorAssignmentVacation r2
                    where 
                        r.ppId = r2.ppId
                        AND r2.TYPE = 'BOUNDED_ASSIGNMENT'
                        AND r2.dateEnd < @date
            )
),
historicPreviousVacation (aId, ppId, unitId) AS (
    select r.aId, r.ppId, r.unitId
        from @historicSectorAssignmentVacation r
        where 
            r.ppId NOT IN (SELECT s.ppId FROM currentTempOrVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM permanent s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicTemp s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM nextVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM historicNextVacation s)
            AND r.ppId NOT IN (SELECT s.ppId FROM previousVacation s)
            AND r.TYPE = 'VACATION'
            AND r.dateEnd = (
                SELECT MAX(r2.dateEnd)
                    from @historicSectorAssignmentVacation r2
                    where 
                        r.ppId = r2.ppId
                        AND r2.TYPE = 'VACATION'
                        AND r2.dateEnd < @date
            )
),
activeAssignments (aId, ppId, unitId) AS (
    (SELECT * FROM currentTempOrVacation) UNION
    (SELECT * FROM historicTemp) UNION
    (SELECT * FROM historicVacation) UNION
    (SELECT * FROM permanent) UNION
    (SELECT * FROM nextVacation) UNION
    (SELECT * FROM historicNextVacation) UNION
    (SELECT * FROM previousVacation) UNION
    (SELECT * FROM historicPreviousVacation)
),
result (id, casualName, lastName, firstName, birthDay, gender, status, attendance, vacation, unitName) AS (
    select
        aa.aId, p.casualName, p.lastName, p.firstName, p.birthDay, p.gender, pp.patientStatus as status, pp.attendance, pp.vacation, u.name as unitName
        from activeAssignments aa
            inner join MainPatient pp on aa.ppId=pp.id
            inner join Patient p on pp.patient_id=p.id
            inner join unit u on u.id=aa.unitId
            where
                1 = 1
)
    select
         r.id, r.casualName, r.lastName, r.firstName, r.birthDay, r.gender, r.status, r.attendance, r.vacation, r.unitName
        from result r