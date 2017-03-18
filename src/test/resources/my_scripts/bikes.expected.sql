select
		b.name,
		s.height,
			tf.width as front_tire,
			tr.width as rear_tire
	from
		motorcycle b
		inner join specification s on b.id=s.fk_vehicule
			inner join tire tf on tf.id=s.fk_front_tire
			inner join tire tr on tr.id=s.fk_rear_tire
	where s.length < 1500;