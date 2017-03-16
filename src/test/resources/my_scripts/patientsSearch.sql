/*
Parametres de remplacement
o    archive = si on ne recherche que les archivés
o    vacation = si on ne recherche que les vacanciers
o    count = si on veut le décompte des patients de la recherche, sinon on a la liste
o    birthday = si le filtrage doit être fait sur la date de naissance
o    statusNormal = si on recherche les patients en état normal (si oui, le filtre par status est ignoré), inverse de notGood
o    statusGood = si on recherche les patients en état bon (si oui, le filtre par status est ignoré), inverse de notNormal
o    status = si on recherche un status en particulier inverse de (normal & good)
o    doctorId = si on recherche par docteur
o    unitId = si on recherche par unité
o    recordNumber = si on recherche un main patient particulier
o    name = si on recherche par nom de famille ou par nom usuel
o    firstName = si on recherche par prénom
o    gender = si on recherche par genre
o    attendance =  si on recherche par présence
o    firstResult = si on doit paginer le resultat, debut de prise en compte (la variable doit contenir l'index de debut ou offset, ex : a partir de 5)
o    maxResults = si on doit paginer le resultat, fin de prise en compte (la variable doit contenir le nombre a retourner ou limit, ex : 10 resultats)
o    orderBy = si on tri (la variable doit contenir le nom de la colonne sans alias et le sens du tri, ex : 'firstName ASC')
*/

/* PARSEUR (fonctionnement general)
 * 
 * Infos comment marche le parseur : la valeur est {valeurNonNull ?? non null :: null } ==> si valeurNonNull non null (notez le double espace) : 'la valeur est  non null ' sinon 'la valeur est  null '
 * ou : la valeur est {valeurNonNull ?? non null } ==> si valeurNonNull non null (notez le double espace) : 'la valeur est  non null ' sinon 'la valeur est '
 * 
 * pour ecrire le contenu une variable {valeurNonNull}
 * l'écriture du contenu est possible dans les résulats (valeurNonNull = 5) : la valeur est {valeurNonNull ?? {valeurNonNull} :: null } ==> la valeur est 5
 * 
 * l'imbrication avec X niveaux est possible, ex : {test ?? {test2 ?? 1 :: 2} :: 0}
 * 
 * les conditions peuvent utilisées les ou '||' et les et '&&' ainsi que les non '!'
 * ex : {(v1 && !v2) || (!v1 && v3) ?? valeur}
 */

-- Declaration of variables
declare @date as DATE = ?
{status ?? declare @status as VARCHAR(255) = ? }
{attendance ?? declare @attendance as VARCHAR(255) = ? }
{!vacation ?? declare @sectorId as NUMERIC(19,0) = ? }
{doctorId ?? declare @doctorId as NUMERIC(19,0) = ? }
{unitId ?? declare @unitId as NUMERIC(19,0) = ? }
{recordNumber ?? declare @recordNumber as NUMERIC(19,0) = ? }
{name ?? declare @name as VARCHAR(250) = ? }
{firstName ?? declare @firstName as VARCHAR(250) = ? }
{birthday ?? declare @birthDay as DATE = ? }
{gender ?? declare @gender as INT = ? }

-- Declaration of tables
declare @pPatients TABLE (id BIGINT, vacation BIT);
declare @sectorAssignment TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @historicSectorAssignment TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @sectorAssignmentVacation TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));
declare @historicSectorAssignmentVacation TABLE (aId BIGINT, ppId BIGINT, type varchar(31), dateStart DATE, dateEnd DATE, vacation BIT, unitId NUMERIC(19,0));

-- Insert data in temporary tables
INSERT INTO @pPatients
select id, vacation
    from MainPatient pp
    where
        ISNULL(pp.archive, 0) {archive && !vacation ?? = :: != } 1
        AND ISNULL(pp.merged, 0) = 0
        
        {vacation ?? AND pp.vacation = 1}
        
        {statusNormal ?? AND pp.patientStatus IN ('ALLERGY', 'OPHTHALMOLOGY')}
        {statusGood ?? AND pp.patientStatus != 'ALLERGY' AND pp.patientStatus != 'OPHTHALMOLOGY'}
        {status ?? AND pp.patientStatus = @status}
        
        {attendance ?? AND pp.attendance = @attendance};

INSERT INTO @sectorAssignment
select a.id, pp.id, a.DTYPE, a.dateStart, a.dateEnd, pp.vacation, a.unit_id
        from assignment a
        inner join @pPatients pp on pp.id=a.MainPatient_id
        {!vacation ??
            left join pp_sharedsectors ppss on pp.id = ppss.pp_id
            where
                (a.sector_id = @sectorId OR ppss.sector_id = @sectorId)
                
                {doctorId ?? AND a.doctorReferent_id = @doctorId}

                {unitId ?? AND a.unit_id = @unitId}
        ::
            where
                1 = 1
                
                {doctorId ?? AND a.doctorReferent_id = @doctorId}
        
                {unitId ?? AND a.unit_id = @unitId}
        };

INSERT INTO @historicSectorAssignment
select a.id, pp.id, a.TYPE, a.dateStart, a.dateEnd, pp.vacation, a.unit_id
        from HistoricAssignment a
        inner join @pPatients pp on pp.id=a.MainPatient_id
        {!vacation ??
            left join pp_sharedsectors ppss on pp.id = ppss.pp_id
            where
                (a.sectorId = @sectorId OR ppss.sector_id = @sectorId)
                
                {doctorId ?? AND a.doctorReferent_id = @doctorId}

                {unitId ?? AND a.unit_id = @unitId}
        ::
            where
                1 = 1
                
                {doctorId ?? AND a.doctorReferent_id = @doctorId}
        
                {unitId ?? AND a.unit_id = @unitId}
        };

INSERT INTO @sectorAssignmentVacation
select * from @sectorAssignment r where r.vacation = 1;

INSERT INTO @historicSectorAssignmentVacation
select * from @historicSectorAssignment r where r.vacation = 1;

-- The main query
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
            
                {recordNumber ?? AND p.id = @recordNumber}
                
                {name ?? AND (p.casualName LIKE @name OR p.lastName LIKE @name)}
                {firstName ?? AND p.firstName LIKE @firstName}
                
                {birthday ?? AND CAST(p.birthDay AS DATE) = CAST(@birthDay AS DATE)}
                
                {gender ?? AND p.gender = @gender}
)

{count ??
    select COUNT(DISTINCT r.id) from result r
::
    select
        {maxResults ?? TOP {maxResults}} r.id, r.casualName, r.lastName, r.firstName, r.birthDay, r.gender, r.status, r.attendance, r.vacation, r.unitName
        from result r
        {firstResult ??
            where r.id NOT IN (
                select
                    TOP {firstResult} r2.id
                    from result r2
                    {orderBy ?? ORDER BY r2.{orderBy}}
            )
        }
        {orderBy ??
            ORDER BY r.{orderBy}
        }
}