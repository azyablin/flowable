
CREATE  procedure [calc].[p_calc_dogovor_debt_all_0_rest] @id_dogovor int, @not_calc_bonus int = 0
as
set nocount on

set @not_calc_bonus = coalesce(@not_calc_bonus, 0)
declare @tmp_datestop smalldatetime, @D_DATEINPUT smalldatetime, @summa_dog money,
@id_DopSogl int, @stavka money, @I_DAYS int, @is_close int,
@start_date smalldatetime,  @finish_date smalldatetime, @rest_debt money, @max_id int,
@is_calc_pay int, @id int, @date_now smalldatetime,   @tsmi_kol_days int, @daily_debt money, @debt money,
@op_list varchar(500), @id_smi int, @acc_type int, @op_list_closed varchar(500),
@op_list_dogovor varchar(500),  @op_list_DopSogl varchar(500),  @op_list_free varchar(500), @free_days int,
@curr_summa money, @tmp_dateend smalldatetime,
@grace_period_stavka money, --ставка льготного периода (например до просрочки)
@base_dop_sogl_id int, --id основного допю соглашения, заменющего условия договора. Например бесплатный займ
@full_stavka money, @inc_@free_days int, @is_rest_zaim_up int, @curr_is_rest_zaim_up int,
@counter int, @shtraf_date smalldatetime, @count_days_shtraf int, @exp_date smalldatetime, @first_exp_date date,
@prev_sum money, @dop_sogl_date smalldatetime, @prev_id_DopSogl int,  @is_add_shtraf int, @shtraf_proc_date smalldatetime,
@date_after_pause smalldatetime, @ret_id int, @tmp_date smalldatetime, @debt_type varchar(80), @pay_order int,
@pause_base_sum money, @max_zaim_up_date smalldatetime,  @debt_subtype varchar(80)
set @acc_type = 0
set @rest_debt = 1

set @counter = 0
-- set @free_days = [dbo].[FreeDog](@id_dogovor)

set @date_now = getdate()
create table #tmp_pay (id int identity(1,1), D_DATEINPUT SMALLDATETIME, M_SUMMA MONEY, ID_PARENT int, id_pay_doc int, id_type int, pay_order int, debt_type varchar(80) null)
declare @tmp_Restructuring as table (id int, D_CANCEL date, start_date date, restruct_group_id int)
declare @tmp_Restructuring_date as table ( D_Restructuring date);
declare @tmp_Restructuring_all as table (ID int IDENTITY(1, 1) NOT NULL,
                                         ID_DOG int NULL,
                                         ID_TYPE int NULL,
                                         NPP int NULL,
                                         D1 smalldatetime NULL,
                                         M_SUMMA money NULL,
                                         IS_STORNO int NULL,
                                         D_CANCEL smalldatetime NULL);

declare @tmp_dogovor_debt_history as table (
      [ID] int,
      [ID_DOGOVOR] int NOT NULL,
      [debt] money NOT NULL,
      [rate] money DEFAULT 0 NOT NULL,
      [pay_sum] money DEFAULT 0 NOT NULL,
      [start_date] smalldatetime NOT NULL,
      [finish_date] smalldatetime NOT NULL,
      [debt_type] varchar(80) COLLATE Cyrillic_General_CI_AS NULL,
      [daily_debt] money NULL,
      [pay_order] int NULL,
      [ID_DOPSOGL] int NULL,
      [id_smi] int NULL,
      [acc_type] int NULL,
      [id_source] int NULL,
      [base_sum] money NULL,
      debt_comment varchar(500) NULL,
      debt_subtype varchar(80) NULL
);



insert into @tmp_Restructuring_all (ID_DOG, ID_TYPE,    NPP,   D1,  M_SUMMA,   IS_STORNO , D_CANCEL )
  select ID_DOG, ID_TYPE,    NPP,   D1,  M_SUMMA,   IS_STORNO , D_CANCEL from
    Restructuring r (nolock) where  r.ID_DOG=@id_dogovor and r.ID_TYPE=1 and r.IS_STORNO=2 and r.NPP=0
                                    and r.D_CANCEL is not null;


insert into @tmp_Restructuring_all (ID_DOG, ID_TYPE,    NPP,   D1,  M_SUMMA,   IS_STORNO , D_CANCEL )
  select   t.id_dogovor, 1 ID_TYPE, 0 NPP, cast(t.D_R1 + 1  as date) D1, 0 M_SUMMA, 0 IS_STORNO,   cast(t.D_CANCEL as date) from tSmi t(nolock)
  where D_CANCEL is not null and t.id_dogovor = @id_dogovor
        and t.ID_STATUS = 4;


with rs as (
    select cast(r.D1  - 1 as date) D1,  cast(r.D_CANCEL as date)  D_CANCEL from
      @tmp_Restructuring_all r
)
insert into @tmp_Restructuring_date(D_Restructuring)
  select D1 from rs
  union
  select D_CANCEL from rs;




delete from #tmp_dogovor_debt where id_dogovor = @id_dogovor and  acc_type = @acc_type
delete from #tmp_dogovor_debt_pay where id_dogovor = @id_dogovor and  acc_type = @acc_type

/* where id_type <> 6*/


/*TODO учесть @is_close*/
set @inc_@free_days = 1
select @is_close = is_close from dogovor_correction dc (nolock)   where dc.id_dogovor = @id_dogovor and dc.doc_type = 0;
set @is_close = COALESCE(@is_close, 0)

select top 1 @base_dop_sogl_id = dp.id from  dopsogl dp (nolock)
  inner join Dogovor  d (nolock) on dp.ID_dog = d.id and cast(dp.D_DATEINPUT as date)  = cast(d.D_DATEINPUT as date)
                                    and d.id = @id_dogovor and (d.D_DATEINPUT < '20190226' or coalesce(dp.M_SUMMA, 0) = 0)
                                    and dp.ID_TYPE not in (70, 72, 73);

set @base_dop_sogl_id = COALESCE(@base_dop_sogl_id, 0)

select top 1 @tmp_datestop= cast(coalesce(D_DATESTOP, d.D_DATE_END, @date_now ) as date),   @summa_dog = d.M_SUMMA,
  @stavka=d.I_STAVKA , @tmp_dateend =   cast(D_DATE_END as date) , @I_DAYS = coalesce(dp.I_DAYS, d.I_DAYS),
  @D_DATEINPUT = d.D_DATEINPUT
from Dogovor d (nolock)
  left join  dopsogl dp (nolock) on dp.ID_DOG = d.ID and dp.id = @base_dop_sogl_id
where d.ID=@id_dogovor

insert into #tmp_pay(D_DATEINPUT, M_SUMMA,ID_PARENT, id_pay_doc, id_type, pay_order, debt_type )
  select D_DATEINPUT, M_SUMMA,ID_PARENT, id, id_type, pay_order, debt_type   from dbo.get_dogovor_pay_for_calc(@id_dogovor)
  where D_DATEINPUT is not null and (@not_calc_bonus = 0 or id_type <> 6)

--Флаг начисления процентов с остатка по телу займа
set @is_rest_zaim_up = 0
-- if @D_DATEINPUT >= '20160606'
set @is_rest_zaim_up = 1;
set @full_stavka = @stavka
select @free_days = idays, @grace_period_stavka =   i_proc from [dbo].[FreeDog](@id_dogovor)
if  @grace_period_stavka > 0
  set @inc_@free_days  = 1

set @grace_period_stavka = coalesce(@grace_period_stavka, 0)
set @free_days = coalesce(@free_days, 0)

if @free_days >  @I_DAYS and @grace_period_stavka > 0
  set @I_DAYS = @free_days
else
  set @I_DAYS = null

declare  @tmp_dog   table(id int, D_DATEINPUT date, I_DAYS int, id_DopSogl int, op_list varchar(500), id_smi int, m_summa money, stavka money , pay_order int,
debt_subtype varchar(80), descr varchar(255) ) ;
declare  @tmp_debt   table(descr varchar(255),debt_type varchar(255),D_DATEINPUT date, I_DAYS int, id_DopSogl int, op_list varchar(500),
                           id_smi int, m_summa money, stavka money, is_delay int, sort_prior int default 10000,  id_type_dop_sogl int,
                            restruct_group_id int null , pay_order  int, debt_subtype varchar(80) ) ;


set   @op_list_closed =    ',ZAIM_UP,PROC_ZAIM_DAYLY_UP,';
set @op_list_dogovor = ',ZAIM_UP,PROC_ZAIM_DAYLY_UP,PROC_ZADOL_UP,SHTRAF_UP,'
set @op_list_DopSogl  = ',PROC_ZAIM_DAYLY_UP,PROC_ZADOL_UP,SHTRAF_UP,';

if @grace_period_stavka > 0
  set @op_list_free = ',PROC_ZAIM_FREE_GRACE_UP,';
else
  set @op_list_free = ',PROC_ZAIM_FREE_UP,';
set @free_days = coalesce(@free_days, 0)
--  set @free_days = 0




set @base_dop_sogl_id = coalesce(@base_dop_sogl_id, 0);
--REPLACE(op_list, ',ZAIM_UP,', ',')
--Начисление до бесплатного займа
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi)
  select  'Начисление до бесплатного займа','dog__before_free',  dp.D_DATEINPUT  ,  @free_days + @inc_@free_days - 1,   dp.id id_DopSogl,
                                                                                                                        case when @is_close > 0 then @op_list_closed else @op_list_dogovor  end op_list , coalesce(d.M_SUMMA, dp.M_SUMMA), @grace_period_stavka, 0
  from dogovor d  (nolock)
    inner join  dopsogl dp (nolock) on dp.ID_DOG = d.ID and dp.id = @base_dop_sogl_id
  where d.id = @id_dogovor   and @free_days > 0;



--Бесплатный займ
insert into @tmp_debt(sort_prior, descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi)
  select 0, 'Бесплатный займ',  'free',DATEADD(day, @free_days + @inc_@free_days , dp.D_DATEINPUT) -1,  @free_days + @inc_@free_days I_DAYS,   dp.id id_DopSogl,
                                                                                                        @op_list_free op_list , coalesce(d.M_SUMMA, dp.M_SUMMA) M_SUMMA, @grace_period_stavka stavka, 0
  from dogovor d  (nolock)
    inner join  dopsogl dp (nolock) on dp.ID_DOG = d.ID and dp.id = @base_dop_sogl_id
  where d.id = @id_dogovor and @free_days > 0;

/*  select 'Начисления по договору после бесплатного займа  ',@I_DAYS,  dp.I_DAYS,   (@free_days + @inc_@free_days) + 1 ,
   coalesce(@I_DAYS, dp.I_DAYS) - (@free_days + @inc_@free_days) + 1
   from dogovor d  (nolock)
   inner join  dopsogl dp (nolock) on dp.ID_DOG = d.ID and dp.id = @base_dop_sogl_id
   where d.id = @id_dogovor   and @free_days > 0  ;*/

--Начисления по договору после бесплатного займа
insert into @tmp_debt(descr,debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi)
  select 'Начисления по договору после бесплатного займа  ', 'dog_free', DATEADD(day, @free_days + @inc_@free_days , dp.D_DATEINPUT) - 1   ,
    coalesce(@I_DAYS, dp.I_DAYS) - (@free_days + @inc_@free_days) + 1,   dp.id id_DopSogl,
                                                                         replace(case when @is_close > 0 then @op_list_closed else @op_list_dogovor end , ',ZAIM_UP,', ',') op_list,

    coalesce(d.M_SUMMA, dp.M_SUMMA), @stavka, 0
  from dogovor d  (nolock)
    inner join  dopsogl dp (nolock) on dp.ID_DOG = d.ID and dp.id = @base_dop_sogl_id
  where d.id = @id_dogovor   and @free_days > 0  ;

--Начисления по договору и допникам без бесплатного займа

with dog as (
  select 'Начисления по договору и допникам без бесплатного займа  dog ' descr, D_DATEINPUT  ,  I_DAYS,   0 id_DopSogl,
         case when @is_close > 0 then @op_list_closed else @op_list_dogovor end op_list , d.M_SUMMA, @stavka stavka, 0 id_type_dop_sogl
  from dogovor d  (nolock)
  where d.id = @id_dogovor and @free_days = 0
  union all
  select  'Начисления по договору и допникам без бесплатного займа  DopSogl ' descr,  D_DATEINPUT ,   I_DAYS,  id,
          case when M_SUMMA > 0 then  @op_list_DopSogl  else  @op_list_DopSogl end op_list,
    M_SUMMA,  coalesce(ds.i_stavka, @stavka), ds.id_type id_type_dop_sogl from   DopSogl (nolock) ds
  where ID_DOG=@id_dogovor and id <> @base_dop_sogl_id
  union all
  select  'Начисления поcле допника с процентами ' descr, DATEADD(day, I_DAYS, D_DATEINPUT ) D_DATEINPUT, 0 I_DAYS,  id,
          case when M_SUMMA > 0 then  @op_list_DopSogl  else  @op_list_DopSogl end +
          case ds.id_type when 8 then ',PROC_UP_DELAY,' else '' end   op_list,
    M_SUMMA, case when ds.ID_TYPE in (70, 72, 73)   then ds.i_stavka  else  @stavka end, 0 id_type_dop_sogl from   DopSogl (nolock) ds
  where ID_DOG=@id_dogovor and id <> @base_dop_sogl_id and ds.i_stavka is not null

  /*  union all
--Разница тела займа для частичного погашения

select  'Начисления по договору и допникам без бесплатного займа  Разница тела займа для частичного погашения ' descr,  D_DATEINPUT , 0  I_DAYS,  id,
  ',ZAIM_UP,'  op_list,
 M_DELTA_SUMMA, @stavka from   dbo.dop_sogl_delta_sum(@id_dogovor)
 where ID_DOG=@id_dogovor and id <> @base_dop_sogl_id      */
)
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, id_type_dop_sogl)
  select   descr, 'dog' debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, 0 id_smi, id_type_dop_sogl
  from dog
  union all
  select 'Начисления по договору и допникам без бесплатного займа  stop ', 'stop', coalesce(  @tmp_dateend, @tmp_datestop  ), 0, 0 , '', null, null, 0, 0
;


insert into @tmp_debt(sort_prior, descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi)
--Разница тела займа для частичного погашения

  select 0 sort_prior, 'Начисления по договору и допникам без бесплатного займа  Разница тела займа для частичного погашения ' descr,
         'dop_sogl_delta_sum' debt_type, D_DATEINPUT , 0  I_DAYS,  id,
         ',ZAIM_UP,DOP_SOGL_DELTA_SUM,'  op_list,
    M_DELTA_SUMMA, @stavka, 0 from   dbo.dop_sogl_delta_sum(@id_dogovor)
  where ID_DOG=@id_dogovor and id <> @base_dop_sogl_id

--Дата штрафа
if @D_DATEINPUT >= '20220804'  BEGIN

  insert into @tmp_dogovor_debt_history
  select [ID],[ID_DOGOVOR] , [debt] , [rate] ,  [pay_sum], [start_date], [finish_date],  [debt_type], [daily_debt],
        [pay_order],  [ID_DOPSOGL] , [id_smi],  [acc_type], [id_source],  [base_sum] ,
        debt_comment ,  debt_subtype
        from dogovor_debt where  ID_DOGOVOR = @id_dogovor and  acc_type = 0 and debt_type = 'SHTRAF_UP' and debt != 0
 set @count_days_shtraf = 1

end
else if @D_DATEINPUT < '20200801'  BEGIN
   select  @count_days_shtraf = penalty_day_count   from dbo.get_common_contract_attrs(@D_DATEINPUT, @D_DATEINPUT)
end
else
begin
  SET  @count_days_shtraf = 26
  select  @shtraf_date = cast( te.D_DATE as  date) from  tExpiredContract te where te.ID_DOGOVOR = @id_dogovor
end
select top 1 @exp_date =   cast(D_DATEINPUT as date),  @shtraf_date = COALESCE(@shtraf_date, DATEADD(day, I_DAYS + @count_days_shtraf,  D_DATEINPUT)),
    @shtraf_proc_date = DATEADD(day, I_DAYS ,  D_DATEINPUT)    from @tmp_debt
  where debt_type in ('dog', 'dog_free') and is_delay is null and  coalesce(id_type_dop_sogl, 0) not in (8, 11)
        and op_list like '%,SHTRAF_UP%,' and coalesce(id_dopsogl, 0) not in  (select id from #tmp_credit_vacation)
  order by  DATEADD(day, I_DAYS,  D_DATEINPUT)  desc;
set @shtraf_date = cast (@shtraf_date as date)


if @shtraf_date > @tmp_datestop begin
  set @shtraf_date = '20500101';
end
else begin

  --@exp_date не попадает на кредитные каникулы. TODO костыль, надо продумать
  if (not exists   (select  1 from   @tmp_debt where  D_DATEINPUT = @exp_date and op_list like '%,SHTRAF_UP%,' and id_DopSogl in (select id from #tmp_credit_vacation))) begin

    --начисляем только на тело займа день штрафа
    insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
      select 'начисляем только на тело займа день штрафа ', 'dog' debt_type, @shtraf_date - 1 D_DATEINPUT, 0, id_DopSogl,  ',PROC_ZADOL_UP,', m_summa, stavka, 0 id_smi, 1     from
        @tmp_debt where  D_DATEINPUT = @exp_date and op_list like '%,SHTRAF_UP%,'

    --начисляем после штрафа
    insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
      select 'начисляем после штрафа', 'dog' debt_type, @shtraf_date  D_DATEINPUT, DATEDIFF(day, @shtraf_date , @tmp_datestop), id_DopSogl,  ',PROC_ZADOL_UP,', m_summa, stavka, 0 id_smi, 1     from
        @tmp_debt where  D_DATEINPUT = @exp_date    and op_list like '%,SHTRAF_UP%,'
  end
  else
  begin

    --начисляем только на тело займа день штрафа
    insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
      select  top 1 'начисляем только на тело займа день штрафа ', 'dog' debt_type, @shtraf_date - 1 D_DATEINPUT, 0, id_DopSogl,  ',PROC_ZADOL_UP,', m_summa, stavka, 0 id_smi, 1     from
        @tmp_debt where  D_DATEINPUT <= @shtraf_date and op_list like '%,SHTRAF_UP%,' and id_DopSogl not in (select id from #tmp_credit_vacation) order by D_DATEINPUT desc
    --начисляем после штрафа
    insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
      select top 1 'начисляем после штрафа', 'dog' debt_type, @shtraf_date  D_DATEINPUT, DATEDIFF(day, @shtraf_date , @tmp_datestop), id_DopSogl,  ',PROC_ZADOL_UP,', m_summa, stavka, 0 id_smi, 1     from
        @tmp_debt where  D_DATEINPUT <= @shtraf_date    and op_list like '%,SHTRAF_UP%,' and id_DopSogl not in (select id from #tmp_credit_vacation) order by D_DATEINPUT desc
  end


end


--Записи для просрочки
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
  select 'Записи для просрочки', debt_type,DATEADD(day, I_DAYS,  D_DATEINPUT)   D_DATEINPUT, DATEDIFF(day, D_DATEINPUT, @shtraf_date - 1), id_DopSogl,
    REPLACE( REPLACE(op_list, ',ZAIM_UP,', ','), ',PROC_ZAIM_DAYLY_UP,', ',')   , m_summa, stavka, id_smi, 1 from @tmp_debt t1
  where debt_type in ('dog', 'dog_free') and is_delay is null and  D_DATEINPUT = @exp_date
        and  not exists
  (select 1 from @tmp_debt t2 where DATEADD(day, t1.I_DAYS,  t1.D_DATEINPUT) = t2.D_DATEINPUT and t2.debt_type in ('dog', 'dog_free')
  );


--Записи внутри срока действия
update @tmp_debt set  op_list =  REPLACE( REPLACE(op_list, ',SHTRAF_UP123,', ','), ',PROC_ZADOL_UP,', ',')    where debt_type in ('dog', 'dog_free',  'dog__before_free', 'dog_free', 'free', 'dop_sogl_delta_sum' ) and is_delay is null;


--Оплаты внутри срока действия
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
  select  distinct 'Оплаты внутри срока действия',  debt_type, p.D_DATEINPUT,
    case
    when I_DAYS - datediff(day, p.D_DATEINPUT, c.D_DATEINPUT )  > 0 then I_DAYS - datediff(day, p.D_DATEINPUT, c.D_DATEINPUT )
    else 0
    end I_DAYS, id_DopSogl,

    REPLACE( REPLACE(op_list, ',SHTRAF_UP,', ','), ',ZAIM_UP,', ',') op_list, m_summa, stavka, id_smi, is_delay
  from (
         select t.*, lead(D_DATEINPUT) over (partition by null order by D_DATEINPUT) end_date
         from @tmp_debt t   where debt_type in ('dog', 'dog_free', 'dog__before_free') and is_delay is null ) c
    inner join
    (
      select  cast(p.D_DATEINPUT as date) D_DATEINPUT from   #tmp_pay p
      union
      --Дата перед оплатой для вычисления суммы пристоановки осттков на проценты (2К)
      select   cast(p.D_DATEINPUT - 1 as date) D_DATEINPUT from   #tmp_pay p
      --заодно и приостановки
      union
      select D_Restructuring from @tmp_Restructuring_date
      --дополнительные начисления
      union
      select debt_date  from dbo.additional_dogovor_debt where id_dogovor = @id_dogovor
    )
    p on p.D_DATEINPUT  between  DATEADD(day, 1,c.D_DATEINPUT )    and coalesce(end_date, DATEADD(day, I_DAYS,
                                                                                                  c.D_DATEINPUT ) )
  where @is_rest_zaim_up = 1   and debt_type in ('dog', 'dog_free', 'dog__before_free') and is_delay is null
        and p.D_DATEINPUT  not in (select D_DATEINPUT from @tmp_debt) ;


select @first_exp_date = min(c.D_DATEINPUT) from @tmp_debt c where @is_rest_zaim_up = 1   and debt_type in ('dog', 'dog_free') and is_delay = 1

--Оплаты после срока действия
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay)
  select  distinct 'Оплаты после срока действия', debt_type, p.D_DATEINPUT, 0 I_DAYS, id_DopSogl,  REPLACE( REPLACE(op_list, ',SHTRAF_UP,', ','), ',ZAIM_UP,', ',')  , m_summa, stavka, id_smi, is_delay
  from
    (select c.*, lead(D_DATEINPUT) over (partition by null order by D_DATEINPUT) end_date from  @tmp_debt c where @is_rest_zaim_up = 1   and debt_type in ('dog', 'dog_free') and is_delay = 1) c
    inner join
    (
      select distinct  cast(p.D_DATEINPUT  as date) D_DATEINPUT from   #tmp_pay p
      union
      --Дата перед оплатой для вычисления суммы пристоановки остатков на проценты (2К)
      select   cast(p.D_DATEINPUT - 1 as date) D_DATEINPUT from   #tmp_pay p
      --заодно и приостановки
      union
      select D_Restructuring from @tmp_Restructuring_date
       --дополнительные начисления
      union
      select debt_date   from dbo.additional_dogovor_debt where id_dogovor = @id_dogovor
    )
    p on   p.D_DATEINPUT  between  DATEADD(day, 1,c.D_DATEINPUT )    and coalesce(end_date, DATEADD(day, I_DAYS,
                                                                                                    c.D_DATEINPUT ) )   or p.D_DATEINPUT  between  DATEADD(day, 1, @exp_date )  and @first_exp_date
  where   p.D_DATEINPUT  not in (select D_DATEINPUT from @tmp_debt) ;




update @tmp_debt set op_list =  REPLACE(op_list, ',SHTRAF_UP,', ',')  where D_DATEINPUT <> @exp_date
update @tmp_debt set op_list =  REPLACE(op_list, ',PROC_ZADOL_UP,', ',')  where  is_delay is null and  debt_type in ('dog', 'dog_free',  'dog__before_free', 'dog_free', 'free', 'dop_sogl_delta_sum' )
--если бесплатный займ выходит в просрочку
update   @tmp_debt set op_list = replace(op_list, ',PROC_ZAIM_DAYLY_UP,', ',PROC_ZADOL_UP,') where debt_type ='dog_free' and  I_DAYS = 0 and @free_days > 0

--дополнительные начисления без штрафов
insert into @tmp_debt(descr, debt_type, D_DATEINPUT, I_DAYS, id_DopSogl,  op_list, m_summa, stavka, id_smi, is_delay, debt_subtype )
select 'дополнительные начисления', 'dog', debt_date, 1, NULL, ',' + debt_type + ',', debt, 100, null, null, 'additional' from dbo.additional_dogovor_debt
 where id_dogovor = @id_dogovor and debt_type <> 'SHTRAF_UP'


delete from @tmp_dogovor_debt_history
where exists (select 1 from #tmp_credit_vacation   where  start_date between start_credit_vacation and fnish_credit_vacation   )
and
exists (select 1 from #tmp_credit_vacation where  finish_date between start_credit_vacation and fnish_credit_vacation   )


--удаляем всё между КК
if EXISTS(select 1 from #tmp_credit_vacation ) begin
  delete from @tmp_debt where id_DopSogl not in  (select id from #tmp_credit_vacation)
  and exists (select 1 from #tmp_credit_vacation where D_DATEINPUT between start_credit_vacation and fnish_credit_vacation )
  and op_list = ',PROC_ZADOL_UP,' and debt_type = 'dog'

end

--В случае КК корректировка записей после штрафа
 update @tmp_debt set descr = 'начисляем после штрафа',  op_list = REPLACE(op_list, ',PROC_ZAIM_DAYLY_UP,', ',PROC_ZADOL_UP,')   where D_DATEINPUT >= @shtraf_date
   and not exists (select 1 from #tmp_credit_vacation where D_DATEINPUT between start_credit_vacation and fnish_credit_vacation )
 ;


insert into @tmp_dog(id, D_DATEINPUT, I_DAYS, id_DopSogl, op_list, id_smi, m_summa, stavka, pay_order, debt_subtype, descr )
  select row_number() over(partition by null order by cast(D_DATEINPUT as date), COALESCE(id_DopSogl, 0), sort_prior, case when op_list like '%,ZAIM_UP,%' then 0 else 1 end,  I_DAYS desc),
    D_DATEINPUT,  I_DAYS,   id_DopSogl, op_list,   id_smi, M_SUMMA, stavka, pay_order, debt_subtype, descr
  from (
         select   D_DATEINPUT,  I_DAYS,   id_DopSogl, op_list, 0 id_smi, M_SUMMA, stavka, sort_prior,
                                                               0 pay_order, debt_subtype, descr
         from @tmp_debt t /*outer apply (select  case when op_list like '%,PROC_ZADOL_UP,%' then 0 else 0 end r_pay_order, 0 r_sign
           from Restructuring r (nolock) where r.ID_DOG=@id_dogovor and r.ID_TYPE=1 and r.IS_STORNO=2 and r.NPP=0
            and r.D_CANCEL is not null and  r.D1 - 1 = t.D_DATEINPUT ) c*/

       ) c



select @max_id = max(id) - 1 from @tmp_dog
---Если есть даты больше @tmp_datestop, то уменьшает самую первую дату, остальные записи отсеятся
if not exists(select 1 from @tmp_dog where  D_DATEINPUT = @tmp_datestop ) begin
  update @tmp_dog set D_DATEINPUT = @tmp_datestop where D_DATEINPUT > @tmp_datestop and
                                                        id = (select top 1 t1.id from @tmp_dog t1 where
                                                          t1.D_DATEINPUT > @tmp_datestop order by t1.D_DATEINPUT, t1.id)
end


update @tmp_dog set pay_order = 1000001 where id_DopSogl in
                                        (Select id from DopSogl d where d.ID_TYPE  in (70, 73) and d.ID_DOG  = @id_dogovor  )




--Максимальная дата начисления тела займа

select @max_zaim_up_date = max(D_DATEINPUT) from @tmp_dog where op_list like '%,ZAIM_UP,%'



declare cur_debt cursor LOCAL  FORWARD_ONLY STATIC  for
  select  cast(d.D_DATEINPUT as date) start_date,  d.I_DAYS,
    COALESCE(ds.id_DopSogl, d.id_DopSogl) id_DopSogl, cast(d1.D_DATEINPUT as date)  finish_date,
    d.id, COALESCE(ds.op_list, d.op_list) op_list,  d.id_smi, d.m_summa, COALESCE(ds.stavka, d.stavka) stavka, COALESCE(ds.pay_order, d.pay_order) pay_order, d.debt_subtype
  from @tmp_dog d
    inner join @tmp_dog d1 on d.id = d1.id - 1
    --БАГ с последней записью при допсолглашении
    outer apply (select @stavka stavka, 1 pay_order, 0 id_DopSogl, ',PROC_ZADOL_UP,' op_list  from  DopSogl ds (nolock) where ds.id = d.id_DopSogl
    	and ds.ID_TYPE in (70, 72, 73) and ds.I_DAYS > 0   and d.I_DAYS = 0 ) ds
  where cast(d1.D_DATEINPUT as date) <= @tmp_datestop
  order by d.id


update  #tmp_dogovor_debt_info set shtraf_date = @shtraf_date
set @rest_debt = 999999
set @prev_id_DopSogl = 0
OPEN cur_debt
while 1 =1  begin
  FETCH   NEXT FROM cur_debt INTO   @start_date, @I_DAYS, @id_DopSogl, @finish_date, @id, @op_list, @id_smi, @curr_summa, @stavka, @pay_order, @debt_subtype
  IF (@@FETCH_STATUS <> 0) begin

    break;
  end
  select @date_after_pause = date_after_pause from #tmp_dogovor_debt_info

  if @finish_date < @date_after_pause - 1  begin

    select top 1 @start_date = finish_date +1 , @pause_base_sum = base_sum from #tmp_dogovor_debt order by finish_date desc

    if @start_date <= @finish_date
      exec calc.calc_doc_base_sum @id_dogovor,@acc_type, @start_date,  @pause_base_sum OUTPUT
    exec p_debt_daily_up @id_dogovor, @pause_base_sum,  0,
                         0,   0,  0 ,   0,
                         'PAUSE', 'PAUSE', @start_date,  @finish_date, @finish_date ,
                         null,  @acc_type ,   0, 0, null, null,
                         @rest_debt   output, @ret_id  output


    continue;
  end;

  if @start_date < @date_after_pause begin

    set @tmp_date = @date_after_pause - 1
    --  select   @start_date = max(finish_date +1 ), @pause_base_sum = max( case when base_sum > 0 then base_sum else 0 end  )from #tmp_dogovor_debt
    select top 1 @start_date = finish_date +1 , @pause_base_sum = base_sum from #tmp_dogovor_debt order by finish_date desc
    if @start_date <=  @tmp_date
      exec calc.calc_doc_base_sum @id_dogovor,@acc_type, @start_date,  @pause_base_sum OUTPUT
    exec p_debt_daily_up @id_dogovor, @pause_base_sum,  0,
                         0,   0,  0 ,   0,
                         'PAUSE', 'PAUSE', @start_date,  @tmp_date, @tmp_date ,
                         null,  @acc_type ,   0, 0, null, null,
                         @rest_debt   output, @ret_id  output

    set @start_date = @date_after_pause - 1;

  end


  if (@start_date >  @tmp_dateend) begin

    break;
  end
  if (@rest_debt < 0) begin

    break;
  end
  if @debt_subtype is null
 	 set @summa_dog = coalesce(@curr_summa, @summa_dog)
  else begin
    set @summa_dog = @curr_summa
 end
  --Если все проценты оплачены и нет допников на изменение суммы
  if (@rest_debt  = 0 and @max_zaim_up_date <  @start_date) begin

    --Обработка случая, когда допник на увеличение заключен в день погашения договора
    if coalesce(@id_DopSogl, 0) =  0  or   @op_list not like '%,DOP_SOGL_DELTA_SUM,' or @prev_id_DopSogl =  coalesce(@id_DopSogl, 0)
      break
  end

  if (@finish_date > @tmp_dateend ) set @finish_date = @tmp_dateend;

  if (@max_id = @id) or @is_rest_zaim_up = 1 begin
    /*Оплаты нужно разносить только при последней итерации, т.е при последнем допсоглашении ИЛИ процент начисляется с остатка*/
    set @is_calc_pay = 1
  end else begin
    set @is_calc_pay = 0
  end
  /*  if @counter > 0 begin
      set @op_list = REPLACE(@op_list, ',ZAIM_UP,', ',')
    end*/
  set @counter = @counter + 1;

  if @debt_subtype is null
  	set @curr_is_rest_zaim_up = @is_rest_zaim_up
  else
    set @curr_is_rest_zaim_up = 0

  exec calc.p_calc_dogovor_debt_rest
      @id_dogovor,
      @start_date,
      @summa_dog,
      @I_DAYS,
      @stavka,
      @full_stavka,
      @finish_date,
      @id_DopSogl,
      @id_smi,
      @pay_order,
      @is_calc_pay,
      @op_list,
      @acc_type,
      @curr_is_rest_zaim_up,
      @shtraf_date,
      @debt_subtype,
      @rest_debt out


  set @prev_sum = @summa_dog
  set @prev_id_DopSogl =  coalesce(@id_DopSogl, 0)



end


--Если будет пересчёт без бонусов, то выходим
if @rest_debt = -1
  return;
set @start_date = null
-- select  top 1   @start_date =  db.start_date  from #tmp_dogovor_debt db where  db.debt_type = 'PROC_ZADOL_UP'  order by db.start_date

if @shtraf_date <= @tmp_datestop
  set @start_date = @shtraf_date
--if @shtraf_proc_date <= @tmp_datestop
--	set @start_date = @shtraf_proc_date
update #tmp_dogovor_debt set finish_date = cast(finish_date as date) where finish_date <> cast(finish_date as date)
select  @rest_debt = sum(db.debt - db.pay_sum)
from #tmp_dogovor_debt db (nolock)
where db.ID_DOGOVOR = @id_dogovor and acc_type = @acc_type;
if @rest_debt >  0   begin
  /*Распределяем оставшиеся платежи*/
  exec p_dogovor_pay_distr_all @id_dogovor, @date_now, @acc_type, @rest_debt out
end

set @is_add_shtraf = [dbo].[Shtraf2017](@id_dogovor)
if @rest_debt = 0 and  @tmp_dateend is null  begin

  select @tmp_dateend = MAX(pay_date) from #tmp_dogovor_debt_pay
  if @tmp_datestop >  @tmp_dateend
    set @tmp_datestop = @tmp_dateend

end

  set @op_list = ',SHTRAF_UP_PERCENT,';

  set @debt_subtype = 'additional'
  declare cur_shtraf_additional cursor LOCAL  FORWARD_ONLY STATIC  for
   select  debt_date  start_date, debt_date  finish_date,  NULL id_DopSogl, null id_smi,
   	debt curr_summa, 100 stavka, debt_type from dbo.additional_dogovor_debt
 	where id_dogovor = @id_dogovor and debt_type ='SHTRAF_UP'
  OPEN cur_shtraf_additional
  while 1 = 1  begin
    FETCH   NEXT FROM cur_shtraf_additional INTO   @start_date, @finish_date, @id_DopSogl,   @id_smi, @curr_summa, @stavka, @debt_type
    IF (@@FETCH_STATUS <> 0) break;
    set @full_stavka = @stavka
    set @summa_dog = @curr_summa

    exec calc.p_calc_dogovor_debt_rest
      @id_dogovor,
      @start_date,
      @summa_dog,
      @I_DAYS,
      @stavka,
      @full_stavka,
      @finish_date,
      @id_DopSogl,
      @id_smi,
      0,
      0,
      @op_list,
      @acc_type,
      @is_rest_zaim_up,
      @shtraf_date,
      @debt_subtype,
      @rest_debt out

  end


--TODO пареметризовать
set @is_add_shtraf = 0
if @is_add_shtraf > 0 and @start_date is not null begin



  --штраф по начисленным процентам

  set @is_calc_pay = 1
  set @op_list = ',SHTRAF_UP_PERCENT,';


  declare @prev_debt_type varchar(255) = '', @max_restructuring_date smalldatetime, @prev_summa money  , @base_sum_date smalldatetime
  select @max_restructuring_date = max(D_Restructuring) from @tmp_Restructuring_date
  having  max(D_Restructuring ) > (select MAX(finish_date) from #tmp_dogovor_debt ) and max(D_Restructuring )  < @tmp_datestop

  if @max_restructuring_date is null   begin


    declare cur_shtraf cursor LOCAL  FORWARD_ONLY STATIC  for
      with src as (
          select start_date, finish_date, ID_DOPSOGL, id_smi,  base_sum,  @stavka  stavka, debt_type debt_type, pause_type from #tmp_dogovor_debt t
          where  finish_date >  @start_date and  debt_type   in  ('PROC_ZADOL_UP' , 'PAUSE' )
                 and t.base_sum > 0 and case when debt_type =  'PAUSE'  then 1 else debt end > 0
      )
      select   start_date, finish_date, ID_DOPSOGL, id_smi,  base_sum,   stavka, debt_type debt_type from src
      union all
      select * from (
                      select top 1   finish_date + 1 start_date,  @tmp_datestop finish_date,    ID_DOPSOGL, id_smi,  base_sum,   stavka,
                                     case when pause_type is not null then  'PAUSE' else debt_type end debt_type
                      from src where  finish_date
                                      = (select  MAX(finish_date)  from src where  finish_date <= @tmp_datestop )
                    ) c  where  c.start_date <= c.finish_date
      order by 1  ;
  end
  else  begin
    declare cur_shtraf cursor LOCAL  FORWARD_ONLY STATIC  for
      with src as (
          select start_date, finish_date, ID_DOPSOGL, id_smi,  base_sum,  @stavka  stavka, debt_type debt_type, pause_type from #tmp_dogovor_debt t
          where  finish_date >  @start_date and  debt_type   in  ('PROC_ZADOL_UP' , 'PAUSE' )
                 and t.base_sum > 0 and case when debt_type =  'PAUSE'  then 1 else debt end > 0
      )
      select   start_date, finish_date, ID_DOPSOGL, id_smi,  base_sum,   stavka, debt_type debt_type from src
      union all
      select * from (
                      select top 1   finish_date + 1 start_date,  @max_restructuring_date finish_date,    ID_DOPSOGL, id_smi,  base_sum,   stavka,
                                     case when pause_type is not null then  'PAUSE' else debt_type end debt_type
                      from src where  finish_date
                                      = (select  MAX(finish_date)  from src where  finish_date <= @tmp_datestop )
                    ) c      where  c.start_date <= c.finish_date
      union all
      select * from (
                      select top 1   @max_restructuring_date + 1 start_date,  @tmp_datestop finish_date,    ID_DOPSOGL, id_smi,  base_sum,   stavka,
                                     case when pause_type is not null then  'PAUSE' else debt_type end debt_type
                      from src where  finish_date
                                      = (select  MAX(finish_date)  from src where  finish_date <= @tmp_datestop )
                    ) c      where  c.start_date <= c.finish_date
      order by 1  ;
  end

  set @finish_date = @start_date
  OPEN cur_shtraf
  while 1 =1  begin
    FETCH   NEXT FROM cur_shtraf INTO   @start_date, @finish_date, @id_DopSogl,   @id_smi, @curr_summa, @stavka, @debt_type
    IF (@@FETCH_STATUS <> 0) break;

    if (@start_date >  @tmp_dateend and @tmp_dateend is not null) break;

    if @debt_type =   'PAUSE'
      set @stavka = 0.1
    else
      set @stavka = 0.05
    --    set  @prev_debt_type = coalesce(@debt_type, '')
    set @full_stavka = @stavka
    if @debt_type =   'PAUSE' begin
      set @base_sum_date = @start_date - 1
      exec calc.calc_doc_base_sum @id_dogovor,@acc_type, @base_sum_date,  @curr_summa OUTPUT
    end
    if @curr_summa = 0
      set @curr_summa = @prev_summa
    set @summa_dog = coalesce(@curr_summa, @summa_dog)
    set @I_DAYS =  datediff(day, @start_date, @finish_date) + 1

    set @prev_summa = @summa_dog
    if @start_date <= @finish_date begin
      exec calc.p_calc_dogovor_debt_rest
          @id_dogovor,
          @start_date,
          @summa_dog,
          @I_DAYS,
          @stavka,
          @full_stavka,
          @finish_date,
          @id_DopSogl,
          @id_smi,
          0,
          @is_calc_pay,
          @op_list,
          @acc_type,
          @is_rest_zaim_up,
          @shtraf_date,
          null,
          @rest_debt out
    end


  end



  if @finish_date <  @date_now and @finish_date <  @tmp_dateend and @start_date < @tmp_datestop begin
    set @start_date = dateadd(dd, 1, @finish_date)
    set @finish_date = @date_now
    if @finish_date > @tmp_dateend
      set @finish_date = @tmp_dateend
    if @finish_date > @tmp_datestop
      set @finish_date = @tmp_datestop
    select @summa_dog = sum(db.debt - db.pay_sum)
    from #tmp_dogovor_debt db (nolock)
    where db.ID_DOGOVOR = @id_dogovor and acc_type = @acc_type and db.debt_type = 'ZAIM_UP' ;
    set @I_DAYS =  datediff(day, @start_date, @finish_date) + 1

    set @is_calc_pay = 1
    exec calc.p_calc_dogovor_debt_rest
        @id_dogovor,
        @start_date,
        @summa_dog,
        @I_DAYS,
        @stavka,
        @full_stavka,
        @finish_date,
        @id_DopSogl,
        @id_smi,
        0,
        @is_calc_pay,
        @op_list,
        @acc_type,
        @is_rest_zaim_up,
        @shtraf_date,
        null,
        @rest_debt out
  end
end;

update #tmp_dogovor_debt set id_dopsogl = null where id_dopsogl  in (select id from #tmp_credit_vacation)
and not  exists (select 1 from #tmp_credit_vacation where start_date between start_credit_vacation and fnish_credit_vacation )

delete from #tmp_dogovor_debt where debt_type = 'PAUSE'
/*Распределяем оставшиеся платежи*/
if coalesce(@rest_debt, 0) > 0
  exec p_dogovor_pay_distr_all @id_dogovor, @date_now, @acc_type, @rest_debt out

--Воспользовался ли клиент бесплатным займом
if (@rest_debt = 0 and @free_days > 0)   begin
  if (not exists (select 1 from #tmp_dogovor_debt db  where db.ID_DOGOVOR =
                                                            @id_dogovor and db.acc_type = @acc_type and db.debt_type <> 'ZAIM_UP' )) begin
    merge into DOG_NUM d
    using
      (select @id_dogovor DOG_ID,  5 ATTRDESC_ID, 1 ATTR_VALUE  ) src
    on (d.DOG_ID = src.DOG_ID and d.ATTRDESC_ID = src.ATTRDESC_ID)
    when matched then
    update set d.ATTR_VALUE = 1
    when not matched then
    insert (DOG_ID, ATTRDESC_ID, ATTR_VALUE)
      values (src.DOG_ID, src.ATTRDESC_ID, src.ATTR_VALUE)  ;
  end
end;




---группировка приостановок
insert into @tmp_Restructuring(id, d_cancel, start_date, restruct_group_id)
  select t.id, r.D_CANCEL , t.start_date,
    max(t.id) over (partition by    r.D_CANCEL, debt_type, pay_order, ID_DOPSOGL,id_smi, debt_subtype, acc_type )
  from #tmp_dogovor_debt t
    cross apply ( select top 1 r.d_cancel, r.id from  @tmp_Restructuring_all r
    where r.D_CANCEL is not null and t.start_date between  cast(r.D1 as date) and cast(r.D_CANCEL as date)
          and t.finish_date between  cast(r.D1 - 1 as date) and cast(r.D_CANCEL as date)) r
  where  t.debt_type <> 'SHTRAF_UP' or  t.debt <> 0



insert into #tmp_dogovor_debt(ID_DOGOVOR, debt, rate, start_date, finish_date, debt_type, daily_debt, pay_order, ID_DOPSOGL, id_smi, acc_type , base_sum,
                              debt_comment,  debt_subtype, pay_sum, restruct_group_id )
  select @id_dogovor, sum(s.debt) debt, max(rate) rate, max(s.d_cancel) start_date,  max(s.d_cancel) finish_date, max(debt_type) debt_type,sum(s.debt) daily_debt,
    max(pay_order), max(ID_DOPSOGL) ID_DOPSOGL, max(id_smi) id_smi, max(acc_type) acc_type, max(base_sum) base_sum, null debt_comment, max(debt_subtype) debt_subtype,
    sum(pay_sum),
    new_restruct_group_id
  from (
         select t.*, r.d_cancel,r.restruct_group_id new_restruct_group_id ,DATEDIFF(DAY, r.start_date, cast(r.d_cancel as date)) + 1 days  from #tmp_dogovor_debt  t
           inner join @tmp_Restructuring r on t.id = r.id)
       s group by s.new_restruct_group_id;


--перенос оплат по приостановкам
merge into #tmp_dogovor_debt_pay td
using (
        select   t.id new_id_dogovor_debt,  d.id  from @tmp_Restructuring r
          inner join #tmp_dogovor_debt t on t.restruct_group_id = r.restruct_group_id
          inner join #tmp_dogovor_debt_pay  d on d.id_dogovor_debt = r.id
      ) src on (td.id  =src.id)
when matched then
update set td.id_dogovor_debt = src.new_id_dogovor_debt;

delete from  #tmp_dogovor_debt where id in (select id from @tmp_Restructuring)

delete from #tmp_dogovor_debt where debt_type =  'SHTRAF_UP' and debt = 0 and id <> (
 select top 1 id  from #tmp_dogovor_debt where debt_type =  'SHTRAF_UP' and debt = 0 order by id_dopsogl desc
)

if @acc_type = 0 begin
  exec [calc].[p_calc_national_tax] @id_dogovor;
    --история по штрафам
  if (exists(select 1 from @tmp_dogovor_debt_history))
  begin
   WITH dest AS (
        select * from #tmp_dogovor_debt d  where d.ID_DOGOVOR = @id_dogovor and  d.acc_type =@acc_type
  )
  merge into  dest  d
  using (
      select t.* from  @tmp_dogovor_debt_history t
       where   acc_type =@acc_type
  ) s on
  s.id_dogovor = d.id_dogovor and s.[start_date] = d.[start_date]
  and coalesce(s.[debt_type], '') = coalesce(d.[debt_type], '')  and  coalesce(s.ID_DOPSOGL,0) = coalesce(d.ID_DOPSOGL,0)
  and d.[id_smi] = s.[id_smi]  and coalesce(s.ID_DOPSOGL, 0) =  coalesce(d.ID_DOPSOGL, 0)
  and  coalesce(s.acc_type, 0) =   coalesce(d.acc_type, 0) and coalesce(s.debt_subtype, '') =   coalesce(d.debt_subtype, '')
   when not matched by target
   then insert
      (
        ID_DOGOVOR,  debt,   rate, pay_sum, start_date,
        finish_date,  debt_type, daily_debt,  pay_order,  ID_DOPSOGL,
        id_smi,  acc_type, base_sum, debt_comment, debt_subtype, id_history
      )
              values
      (
        s.ID_DOGOVOR,  s.debt,   s.rate, s.debt, s.start_date,
        s.finish_date,  s.debt_type, s.daily_debt,  s.pay_order,  s.ID_DOPSOGL,
        s.id_smi,  s.acc_type, s.base_sum, s.debt_comment, s.debt_subtype, s.id
      );


      insert into #tmp_dogovor_debt_pay ( id_dogovor_debt, id_pay_doc, id_pay_type,
              pay_sum, pay_date, id_dogovor, acc_type, id_pay)
      select d.id, c.id, 100, pay_sum,     c.D_DATEINPUT,
        id_dogovor, acc_type, 1 from #tmp_dogovor_debt d
        outer apply (select top 1 dl.id,  dl.D_DATEINPUT, dl.ID_TYPE  from  DopSogl dl  (nolock) where dl.ID_DOG =  d.ID_DOGOVOR
        	and dl.D_DATEINPUT >= d.start_date order by  dl.D_DATEINPUT    ) c
         where id_history in (select id from @tmp_dogovor_debt_history ) and coalesce(c.ID_TYPE, 0) not in (70, 72, 73)  and  c.id is not null
  end
end

exec p_calc_doc_all_save @acc_type
